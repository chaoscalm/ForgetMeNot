package com.odnovolov.forgetmenot.screen.exercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardFragment
import kotlinx.android.synthetic.main.fragment_exercise.*
import leakcanary.LeakSentry

class ExerciseFragment : BaseFragment() {

    private val controller = ExerciseController()
    private val viewModel = ExerciseViewModel()
    private lateinit var speaker: Speaker
    private lateinit var adapter: ExerciseCardsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        speaker = Speaker(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(viewScope!!, ::executeOrder)
    }

    private fun setupView() {
        setupViewPagerAdapter()
        setupControlPanel()
    }

    private fun setupViewPagerAdapter() {
        adapter = ExerciseCardsAdapter(fragment = this)
        exerciseViewPager.adapter = adapter
        exerciseViewPager.offscreenPageLimit = 1
        exerciseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                controller.dispatch(NewPageBecameSelected(position))
            }
        })
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { controller.dispatch(NotAskButtonClicked) }
        undoButton.setOnClickListener { controller.dispatch(UndoButtonClicked) }
        speakButton.setOnClickListener { controller.dispatch(SpeakButtonClicked) }
        editCardButton.setOnClickListener { controller.dispatch(EditCardButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            // we help ViewPager to restore its state
            adapter.exerciseCardIds = exerciseCardsIdsAtStart
            exerciseCardIds.observe { adapter.exerciseCardIds = it }
            isCurrentExerciseCardLearned.observe { isCurrentCardLearned ->
                isCurrentCardLearned ?: return@observe
                notAskButton.visibility = if (isCurrentCardLearned) GONE else VISIBLE
                undoButton.visibility = if (isCurrentCardLearned) VISIBLE else GONE
            }
            levelOfKnowledgeForCurrentCard.observe { levelOfKnowledge: Int? ->
                levelOfKnowledge ?: return@observe
                if (levelOfKnowledge == -1) {
                    levelOfKnowledgeTextView.visibility = GONE
                } else {
                    val backgroundRes = when (levelOfKnowledge) {
                        0 -> R.drawable.background_level_of_knowledge_unsatisfactory
                        1 -> R.drawable.background_level_of_knowledge_poor
                        2 -> R.drawable.background_level_of_knowledge_acceptable
                        3 -> R.drawable.background_level_of_knowledge_satisfactory
                        4 -> R.drawable.background_level_of_knowledge_good
                        5 -> R.drawable.background_level_of_knowledge_very_good
                        else -> R.drawable.background_level_of_knowledge_excellent
                    }
                    levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
                    levelOfKnowledgeTextView.text = levelOfKnowledge.toString()
                    levelOfKnowledgeTextView.visibility = VISIBLE
                }
            }
        }
    }

    private fun executeOrder(order: ExerciseOrder) {
        when (order) {
            MoveToNextPosition -> {
                val nextPosition = exerciseViewPager.currentItem + 1
                exerciseViewPager.setCurrentItem(nextPosition, true)
            }
            is Speak -> {
                speaker.speak(order.text, order.language)
            }
            NavigateToEditCard -> {
                findNavController().navigate(R.id.action_exercise_screen_to_edit_card_screen)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        controller.dispose()
        speaker.shutdown()
        LeakSentry.refWatcher.watch(this)
    }
}


class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var exerciseCardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        val id = exerciseCardIds[position]
        return ExerciseCardFragment.create(id)
    }

    override fun getItemId(position: Int): Long = exerciseCardIds[position]

    override fun containsItem(itemId: Long): Boolean = exerciseCardIds.contains(itemId)

    override fun getItemCount(): Int = exerciseCardIds.size
}
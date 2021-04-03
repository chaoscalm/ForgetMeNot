package com.odnovolov.forgetmenot.presentation.screen.grading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnCorrectAnswer
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnWrongAnswer
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.getGradeChangeDisplayText
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingEvent.*
import kotlinx.android.synthetic.main.fragment_grading.*
import kotlinx.coroutines.launch

class GradingFragment : BaseFragment() {
    init {
        GradingDiScope.reopenIfClosed()
    }

    private var controller: GradingController? = null
    private lateinit var viewModel: GradingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_grading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = GradingDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        firstCorrectAnswerButton.setOnClickListener {
            controller?.dispatch(FirstCorrectAnswerButton)
        }
        firstWrongAnswerButton.setOnClickListener {
            controller?.dispatch(FirstWrongAnswerButton)
        }
        yesAskAgainButton.setOnClickListener {
            controller?.dispatch(YesAskAgainButton)
        }
        noAskAgainButton.setOnClickListener {
            controller?.dispatch(NoAskAgainButton)
        }
        repeatedCorrectAnswerButton.setOnClickListener {
            controller?.dispatch(RepeatedCorrectAnswerButton)
        }
        repeatedWrongAnswerButton.setOnClickListener {
            controller?.dispatch(RepeatedWrongAnswerButton)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            onFirstCorrectAnswer.observe { gradeChange: GradeChangeOnCorrectAnswer ->
                onFirstCorrectAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            onFirstWrongAnswer.observe { gradeChange: GradeChangeOnWrongAnswer ->
                onFirstWrongAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            askAgain.observe { askAgain: Boolean ->
                yesAskAgainButton.isSelected = askAgain
                noAskAgainButton.isSelected = !askAgain
                onRepeatedAnswerGroup.isVisible = askAgain
            }
            onRepeatedCorrectAnswer.observe { gradeChange: GradeChangeOnCorrectAnswer ->
                onRepeatedCorrectAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            onRepeatedWrongAnswer.observe { gradeChange: GradeChangeOnWrongAnswer ->
                onRepeatedWrongAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            GradingDiScope.close()
        }
    }
}
package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.QuestionDisplay
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.HelpButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.QuestionDisplaySwitchToggled

class QuestionDisplayController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<QuestionDisplayEvent, Nothing>() {
    override fun handle(event: QuestionDisplayEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromQuestionDisplay {
                    HelpDiScope(QuestionDisplay)
                }
            }

            QuestionDisplaySwitchToggled -> {
                deckSettings.toggleIsQuestionDisplayed()
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}
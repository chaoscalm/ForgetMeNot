package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.entity.CardReverse
import com.odnovolov.forgetmenot.domain.entity.TestMethod

sealed class DeckSettingsEvent {
    object RandomOrderSwitchToggled : DeckSettingsEvent()
    class TestMethodIsSelected(val testMethod: TestMethod) : DeckSettingsEvent()
    object IntervalsButtonClicked : DeckSettingsEvent()
    object PronunciationButtonClicked : DeckSettingsEvent()
    object DisplayQuestionSwitchToggled : DeckSettingsEvent()
    class CardReverseIsSelected(val cardReverse: CardReverse) : DeckSettingsEvent()
    object PronunciationPlanButtonClicked : DeckSettingsEvent()
    object MotivationalTimerButtonClicked : DeckSettingsEvent()
}
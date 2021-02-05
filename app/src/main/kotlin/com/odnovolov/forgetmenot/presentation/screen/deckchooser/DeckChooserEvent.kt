package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting

sealed class DeckChooserEvent {
    object CancelButtonClicked : DeckChooserEvent()
    class SearchTextChanged(val searchText: String) : DeckChooserEvent()
    object SortingDirectionButtonClicked : DeckChooserEvent()
    class SortByButtonClicked(val criterion: DeckSorting.Criterion) : DeckChooserEvent()
    class DeckButtonClicked(val deckId: Long) : DeckChooserEvent()
}
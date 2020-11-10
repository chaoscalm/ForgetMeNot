package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope

class SearchDiScope(
    initialSearchText: String = ""
) {
    private val cardsSearcher: CardsSearcher =
        if (DeckSetupDiScope.isOpen()) {
            val deck = DeckSetupDiScope.shareDeck()
            CardsSearcher(deck)
        } else {
            val globalState = AppDiScope.get().globalState
            CardsSearcher(globalState)
        }

    val controller = SearchController(
        cardsSearcher,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = SearchViewModel(
        initialSearchText,
        cardsSearcher.state
    )

    companion object : DiScopeManager<SearchDiScope>() {
        override fun recreateDiScope() = SearchDiScope()

        override fun onCloseDiScope(diScope: SearchDiScope) {
            diScope.controller.dispose()
            diScope.cardsSearcher.dispose()
        }
    }
}
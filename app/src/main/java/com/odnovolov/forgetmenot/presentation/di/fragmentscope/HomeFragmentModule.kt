package com.odnovolov.forgetmenot.presentation.di.fragmentscope

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.presentation.screen.DecksPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.binding.HomeFragmentBinding
import dagger.Module
import dagger.Provides

@Module
class HomeFragmentModule {

    @FragmentScope
    @Provides
    fun provideBinding(feature: AddNewDeckFeature): HomeFragmentBinding {
        return HomeFragmentBinding(feature)
    }

    @FragmentScope
    @Provides
    fun provideAdapter(): DecksPreviewAdapter {
        return DecksPreviewAdapter()
    }
}
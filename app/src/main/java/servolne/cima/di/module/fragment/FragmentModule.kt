package servolne.cima.di.module.fragment

import servolne.cima.presentation.home.HomeFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentModule {

    @ContributesAndroidInjector
    fun homeFragment(): HomeFragment
}

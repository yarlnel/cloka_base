package servolne.cima.di.module.navigation

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Cicerone.Companion.create
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NavigationModule {
    private val cicerone: Cicerone<Router> = create()

    @[Provides Singleton]
    fun provideRouter() : Router =
        cicerone.router

    @[Provides Singleton]
    fun provideNavHolder() : NavigatorHolder =
        cicerone.getNavigatorHolder()
}
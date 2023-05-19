package servolne.cima.di.module.app

import servolne.cima.di.module.activity.ActivityModule
import servolne.cima.di.module.fragment.FragmentModule
import servolne.cima.di.module.navigation.NavigationModule
import dagger.Module

@Module(includes = [
    ActivityModule::class,
    FragmentModule::class,
    NavigationModule::class
])
interface AppModule

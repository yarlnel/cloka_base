package servolne.cima.di

import servolne.cima.App
import servolne.cima.di.module.app.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
    ]
)
interface AppComponent : AndroidInjector<App> {

    override fun inject(instance: App)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(instance: App): Builder

        fun build(): AppComponent
    }
}

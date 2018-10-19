package catt.kt.libs.mq.di.modules

import android.content.Context
import catt.kt.libs.mq.core.IConnectionPresenter
import catt.kt.libs.mq.core.MqBinder
import catt.kt.libs.mq.core.MqConnectionPresenter
import catt.kt.libs.mq.di.scopes.ConnectionScope
import dagger.Module
import dagger.Provides

@Module
class ConnectionModule {

    @ConnectionScope
    @Provides
    internal fun providePresenter(context: Context): IConnectionPresenter = MqConnectionPresenter(context)


    @ConnectionScope
    @Provides
    internal fun provideIBinder(presenter: IConnectionPresenter): MqBinder = MqBinder(presenter)
}
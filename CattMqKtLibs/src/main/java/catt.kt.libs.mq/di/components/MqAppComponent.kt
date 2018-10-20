package catt.kt.libs.mq.di.components

import android.content.Context
import catt.kt.libs.mq.di.modules.MqAppModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MqAppModule::class])
interface MqAppComponent {
    fun getContext(): Context
}
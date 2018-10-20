package catt.kt.libs.mq.core

import android.app.Application
import catt.kt.libs.mq.di.components.DaggerMqAppComponent
import catt.kt.libs.mq.di.components.MqAppComponent
import catt.kt.libs.mq.di.modules.MqAppModule

class MqApp : Application() {


    val appComponent: MqAppComponent by lazy {
        DaggerMqAppComponent.builder().mqAppModule(MqAppModule(this@MqApp.applicationContext)).build()
    }


}
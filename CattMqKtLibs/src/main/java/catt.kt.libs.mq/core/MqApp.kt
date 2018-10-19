package catt.kt.libs.mq.core

import android.app.Application
import android.util.Log.i
import catt.kt.libs.mq.di.components.DaggerMqAppComponent
import catt.kt.libs.mq.di.components.MqAppComponent
import catt.kt.libs.mq.di.modules.MqAppModule

class MqApp : Application() {
    private val _TAG: String = MqApp::class.java.simpleName

    private val _component: MqAppComponent by lazy {
        DaggerMqAppComponent.builder().mqAppModule(MqAppModule(this@MqApp.applicationContext)).build()
    }

    override fun onCreate() {
        super.onCreate()
        _component.apply { i(_TAG, "@@ MQ @@ Initialization core component.") }
    }

    val mqAppComponent: MqAppComponent
        get() = _component
}
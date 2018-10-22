package catt.kt.libs.mq.core

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log.e
import catt.kt.libs.mq.di.components.ConnectionComponent
import catt.kt.libs.mq.di.components.DaggerConnectionComponent
import catt.kt.libs.mq.di.modules.ConnectionModule
import javax.inject.Inject

class MqConnectionService : Service() {
    private val _TAG: String = MqConnectionService::class.java.simpleName

    @Inject
    internal lateinit var binder: MqBinder
    @Inject
    internal lateinit var presenter: IConnectionPresenter
    private lateinit var component: ConnectionComponent

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        component = DaggerConnectionComponent.builder()
            .mqAppComponent((application as MqApp).appComponent)
            .connectionModule(ConnectionModule())
            .build()
        component.inject(this@MqConnectionService)

    }

    override fun onDestroy() {
        try {
            presenter.destroyOwn()
            super.onDestroy()
        } finally {
            System.runFinalization()
        }
    }
}

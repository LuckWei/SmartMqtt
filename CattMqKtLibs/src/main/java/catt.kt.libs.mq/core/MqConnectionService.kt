package catt.kt.libs.mq.core

import android.app.Service
import android.content.Intent
import android.os.*
import catt.kt.libs.mq.di.components.ConnectionComponent
import catt.kt.libs.mq.di.components.DaggerConnectionComponent
import catt.kt.libs.mq.di.modules.ConnectionModule
import javax.inject.Inject

class MqConnectionService : Service() {
    private val _TAG: String = MqConnectionService::javaClass.name

    @Inject
    internal lateinit var binder: MqBinder
    @Inject
    internal lateinit var presenter: MqConnectionPresenter

    private lateinit var component: ConnectionComponent

    override fun onBind(intent: Intent?): IBinder? {
        component = DaggerConnectionComponent.builder()
            .mqAppComponent((application as MqApp).mqAppComponent)
            .connectionModule(ConnectionModule())
            .build()
        component.inject(this@MqConnectionService)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        presenter.destroyOwn()
        return super.onUnbind(intent)
    }
}

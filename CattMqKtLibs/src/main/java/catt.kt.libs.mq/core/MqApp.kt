package catt.kt.libs.mq.core

import android.app.Application
import catt.kt.libs.mq.MqConfigure
import catt.kt.libs.mq.di.components.DaggerMqAppComponent
import catt.kt.libs.mq.di.components.MqAppComponent
import catt.kt.libs.mq.di.modules.MqAppModule
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqApp : Application() {

    val appComponent: MqAppComponent by lazy {
        DaggerMqAppComponent.builder().mqAppModule(MqAppModule(this@MqApp.applicationContext)).build()
    }

    val mqClient: MqttAndroidClient by lazy {
        MqttAndroidClient(
            this@MqApp.applicationContext,
            MqConfigure.serverUrl,
            MqConfigure.clientId,
            MemoryPersistence(),
            MqttAndroidClient.Ack.AUTO_ACK
        )
    }
}
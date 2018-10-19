package catt.kt.libs.mq

import android.content.Context
import catt.kt.libs.mq.core.MqServiceConnection
import catt.kt.libs.mq.listeners.*

object MqControl {

    @JvmStatic
    fun bindService(context: Context) {
        MqServiceConnection.get().inject(context).bindServiceOfMessage()
    }

    @JvmStatic
    fun unbindService() {
        MqServiceConnection.get().unbindService()
    }

    @JvmStatic
    val isConnected: Boolean
        get() = MqServiceConnection.get().isConnected

    @JvmStatic
    fun publishMessage(topic: String, message: String) {
        MqServiceConnection.get().publishMessage(topic, message)
    }

    @JvmStatic
    fun subscribe(topic: Array<String>) {
        MqServiceConnection.get().subscribe(topic)
    }

    @JvmStatic
    fun addOnConnectionListener(listener: OnConnectionListener) =
        ConnectionMonitor.get().addOnConnectionListener(listener)

    @JvmStatic
    fun removeOnConnectionListener(listener: OnConnectionListener) =
        ConnectionMonitor.get().removeOnConnectionListener(listener)

    @JvmStatic
    fun addOnPublishDeliveryListener(listener: OnPublishDeliveryListener) =
        PublishDeliveryMonitor.get().addOnPublishDeliveryListener(listener)

    @JvmStatic
    fun removeOnPublishDeliveryListener(listener: OnPublishDeliveryListener) =
        PublishDeliveryMonitor.get().removeOnPublishDeliveryListener(listener)

    @JvmStatic
    fun addOnSubscribeMessagesListener(listener: OnSubscribeMessagesListener) =
        SubscribeMessagesMonitor.get().addOnSubscribeMessagesListener(listener)

    @JvmStatic
    fun removeOnSubscribeMessagesListener(listener: OnSubscribeMessagesListener) =
        SubscribeMessagesMonitor.get().removeOnSubscribeMessagesListener(listener)

}
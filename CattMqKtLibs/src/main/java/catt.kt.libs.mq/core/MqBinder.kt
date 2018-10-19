package catt.kt.libs.mq.core

import android.os.Binder
import catt.kt.libs.mq.core.MqConnectionPresenter
import catt.kt.libs.mq.listeners.OnConnectionListener
import catt.kt.libs.mq.listeners.OnPublishDeliveryListener
import catt.kt.libs.mq.listeners.OnSubscribeMessagesListener

internal class MqBinder(private val _presenter: MqConnectionPresenter) : Binder() {
    val isConnected: Boolean
        get() = _presenter.isConnected

    fun connect() = _presenter.connect()

    fun publishMessage(topic: String, message: String) = _presenter.publishMessage(topic, message)

    fun publishMessage(topic: String, payload: ByteArray) = _presenter.publishMessage(topic, payload)

    fun subscribe(topics: Array<String>) = _presenter.subscribe(topics)
}

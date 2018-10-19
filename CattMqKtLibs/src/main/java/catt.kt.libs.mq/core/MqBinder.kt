package catt.kt.libs.mq.core

import android.os.Binder

internal class MqBinder(private val _presenter: IConnectionPresenter) : Binder() {
    inline val isConnected: Boolean
        get() = _presenter.isConnected()

    fun connect() = _presenter.connect()

    fun publishMessage(topic: String, message: String) = _presenter.publishMessage(topic, message)

    fun subscribe(topics: Array<String>) = _presenter.subscribe(topics)
}

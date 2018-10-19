package catt.kt.libs.mq.core

import catt.kt.libs.mq.wrapper.ServiceConnectionImpl

class MqServiceConnection(
    connectionType: String = MqServiceConnection::class.java.simpleName,
    implicitAction: String = "catt.mq.libs.ACTION_CONNECTION_MQTT",
    ownCategory: String = "k632ioRVbA7xT8sf9RjGlH0v6nBthjmYKQNT49HvjJ0KPPltJRI51tNjdsHzh6nJ"
) : ServiceConnectionImpl(connectionType, implicitAction, ownCategory) {

    private fun connect() {
        binder ?: throw IllegalArgumentException("Service is not connected")
        (binder as MqBinder).connect()
    }

    val isConnected: Boolean
        get() {
            binder ?: return false
            return (binder as MqBinder).isConnected
        }

    fun publishMessage(topic: String, message: String) {
        binder ?: throw IllegalArgumentException("Service is not connected")
        (binder as MqBinder).publishMessage(topic, message)
    }

    fun subscribe(topic: Array<String>) {
        binder ?: throw IllegalArgumentException("Service is not connected")
        (binder as MqBinder).subscribe(topic)
    }

    companion object {
        private object Helper {
            val INSTANCE: MqServiceConnection by lazy { MqServiceConnection() }
        }

        @JvmStatic
        fun get(): MqServiceConnection {
            return Helper.INSTANCE
        }
    }
}
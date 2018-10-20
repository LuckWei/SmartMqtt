package catt.kt.libs.mq.core

import android.content.ComponentName
import android.os.IBinder
import catt.kt.libs.mq.listeners.OnServiceConnectionListener
import catt.kt.libs.mq.wrapper.ServiceConnectionImpl
import java.lang.ref.WeakReference

internal class MqServiceConnection(
    connectionType: String = MqServiceConnection::class.java.simpleName,
    implicitAction: String = "catt.mq.libs.ACTION_CONNECTION_MQTT",
    ownCategory: String = "k632ioRVbA7xT8sf9RjGlH0v6nBthjmYKQNT49HvjJ0KPPltJRI51tNjdsHzh6nJ"
) : ServiceConnectionImpl(connectionType, implicitAction, ownCategory) {

    private var listenerWeakReference: WeakReference<OnServiceConnectionListener>? = null


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

    fun setOnServiceConnectionListener(listener: OnServiceConnectionListener): ServiceConnectionImpl {
        listenerWeakReference = WeakReference(listener)
        return this@MqServiceConnection
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        super.onServiceConnected(name, service)
        if (!isConnected) connect()
        listenerWeakReference?.get()?.onServiceConnected(name)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        super.onServiceDisconnected(name)
        listenerWeakReference?.get()?.onServiceDisconnected(name)
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
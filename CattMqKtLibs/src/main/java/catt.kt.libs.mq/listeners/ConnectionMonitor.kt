package catt.kt.libs.mq.listeners

import android.os.Handler
import android.os.Looper
import android.os.Message


class ConnectionMonitor private constructor() {
    private val _listeners: ArrayList<OnConnectionListener> by lazy { ArrayList<OnConnectionListener>() }

    private val _handler: MonitorHandler by lazy { MonitorHandler(this@ConnectionMonitor) }

    fun addOnConnectionListener(listener: OnConnectionListener?) {
        listener ?: return
        _handler.obtainMessage(CODE_ADD, listener).sendToTarget()
    }

    fun removeOnConnectionListener(listener: OnConnectionListener?) {
        listener ?: return
        _handler.obtainMessage(CODE_REMOVE, listener).sendToTarget()
    }

    internal fun onConnectionLostOfMessage(cause: Throwable) {
        _handler.obtainMessage(CODE_REMOVE, cause).sendToTarget()
    }

    internal fun onConnectingOfMessage(serverURI: String) {
        _handler.obtainMessage(CODE_REMOVE, serverURI).sendToTarget()
    }

    internal fun onConnectCompleteOfMessage(reconnect: Boolean, serverURI: String) {
        _handler.obtainMessage(CODE_REMOVE, CMessage(reconnect, serverURI)).sendToTarget()
    }

    private fun onConnectionLost(cause: Throwable) {
        for (index in 0 until _listeners.size) {
            _listeners[index].onConnectionLost(cause)
        }
    }

    private fun onConnecting(serverURI: String) {
        for (index in 0 until _listeners.size) {
            _listeners[index].onConnecting(serverURI)
        }
    }

    private fun onConnectComplete(cMessage: CMessage) {
        for (index in 0 until _listeners.size) {
            _listeners[index].onConnectComplete(cMessage.reconnect, cMessage.serverURI)
        }
    }

    private fun addListener(listener: OnConnectionListener) {
        _listeners.add(listener)
    }

    private fun removeListener(listener: OnConnectionListener) {
        _listeners.remove(listener)
    }

    companion object Monitor {
        private const val CODE_ADD = 10000
        private const val CODE_REMOVE = 10001
        private const val CODE_ON_CONNECTION_LOST = 10002
        private const val CODE_ON_CONNECTING = 10003
        private const val CODE_ON_CONNECT_COMPLETE = 10004

        private class CMessage(val reconnect: Boolean, val serverURI: String)

        private class MonitorHandler(private val _root: ConnectionMonitor) : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                msg?.apply {
                    when (what) {
                        CODE_ADD -> _root.addListener(obj as OnConnectionListener)
                        CODE_REMOVE -> _root.removeListener(obj as OnConnectionListener)
                        CODE_ON_CONNECTION_LOST -> _root.onConnectionLost(obj as Throwable)
                        CODE_ON_CONNECTING -> _root.onConnecting(obj as String)
                        CODE_ON_CONNECT_COMPLETE -> _root.onConnectComplete(obj as CMessage)
                    }
                }
            }
        }

        private object Helper {
            val INSTANCE: ConnectionMonitor by lazy { ConnectionMonitor() }
        }

        @JvmStatic
        internal fun get(): ConnectionMonitor = Helper.INSTANCE
    }
}
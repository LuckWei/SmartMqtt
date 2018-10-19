package catt.kt.libs.mq.listeners

import android.os.Handler
import android.os.Looper
import android.os.Message


class SubscribeMessagesMonitor private constructor(){
    private val _listeners: ArrayList<OnSubscribeMessagesListener> by lazy { ArrayList<OnSubscribeMessagesListener>() }

    private val _handler: MonitorHandler by lazy { MonitorHandler(this@SubscribeMessagesMonitor) }

    fun addOnSubscribeMessagesListener(listener: OnSubscribeMessagesListener) {
        listener ?: return
        _handler.obtainMessage(CODE_ADD, listener).sendToTarget()
    }

    fun removeOnSubscribeMessagesListener(listener: OnSubscribeMessagesListener) {
        listener ?: return
        _handler.obtainMessage(CODE_REMOVE, listener).sendToTarget()
    }

    internal fun onSubscribeMessageOfMessage(
        topic: String,
        id: Int,
        payload: ByteArray,
        qos: Int,
        repeated: Boolean,
        retained: Boolean
    ) {
        _handler.obtainMessage(CODE_ON_SUBSCRIBE_MESSAGE, SMessage(topic, id, payload, qos, repeated, retained))
            .sendToTarget()
    }

    private fun onSubscribeMessage(sMessage: SMessage) {
        for (index in 0 until _listeners.size) {
            _listeners[index].onSubscribeMessage(
                sMessage.topic,
                sMessage.id,
                sMessage.payload,
                sMessage.qos,
                sMessage.repeated,
                sMessage.retained
            )
        }
    }

    private fun addListener(listener: OnSubscribeMessagesListener) {
        _listeners.add(listener)
    }

    private fun removeListener(listener: OnSubscribeMessagesListener) {
        _listeners.remove(listener)
    }

    companion object Monitor {
        private const val CODE_ADD = 10000
        private const val CODE_REMOVE = 10001
        private const val CODE_ON_SUBSCRIBE_MESSAGE = 10002

        private class SMessage(
            val topic: String,
            val id: Int,
            val payload: ByteArray,
            val qos: Int,
            val repeated: Boolean,
            val retained: Boolean
        )

        private class MonitorHandler(private val _root: SubscribeMessagesMonitor) : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                msg?.apply {
                    when (what) {
                        CODE_ADD -> _root.addListener(obj as OnSubscribeMessagesListener)
                        CODE_REMOVE -> _root.removeListener(obj as OnSubscribeMessagesListener)
                        CODE_ON_SUBSCRIBE_MESSAGE -> _root.onSubscribeMessage(obj as SMessage)
                    }
                }
            }
        }

        private object Helper {
            val INSTANCE: SubscribeMessagesMonitor by lazy { SubscribeMessagesMonitor() }
        }

        @JvmStatic
        internal fun get(): SubscribeMessagesMonitor = Helper.INSTANCE
    }
}
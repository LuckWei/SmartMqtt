package catt.kt.libs.mq.listeners

import android.os.Handler
import android.os.Looper
import android.os.Message


class PublishDeliveryMonitor private constructor(){
    private val _listeners: ArrayList<OnPublishDeliveryListener> by lazy { ArrayList<OnPublishDeliveryListener>() }

    private val _handler: MonitorHandler by lazy { MonitorHandler(this@PublishDeliveryMonitor) }

    fun addOnPublishDeliveryListener(listener: OnPublishDeliveryListener?) {
        listener ?: return
        _handler.obtainMessage(CODE_ADD, listener).sendToTarget()
    }

    fun removeOnPublishDeliveryListener(listener: OnPublishDeliveryListener?) {
        listener ?: return
        _handler.obtainMessage(CODE_REMOVE, listener).sendToTarget()
    }

    internal fun onDeliveryCompleteOfMessage(payload: ByteArray) {
        _handler.obtainMessage(CODE_ON_DELIVERY_COMPLETE, payload).sendToTarget()
    }

    private fun onDeliveryComplete(payload: ByteArray) {
        for (index in 0 until _listeners.size) {
            _listeners[index].onDeliveryComplete(payload)
        }
    }

    private fun addListener(listener: OnPublishDeliveryListener) {
        _listeners.add(listener)
    }

    private fun removeListener(listener: OnPublishDeliveryListener) {
        _listeners.remove(listener)
    }

    companion object Monitor {
        private const val CODE_ADD = 10000
        private const val CODE_REMOVE = 10001
        private const val CODE_ON_DELIVERY_COMPLETE = 10002

        private class MonitorHandler(private val _root: PublishDeliveryMonitor) : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                msg?.apply {
                    when (what) {
                        CODE_ADD -> _root.addListener(obj as OnPublishDeliveryListener)
                        CODE_REMOVE -> _root.removeListener(obj as OnPublishDeliveryListener)
                        CODE_ON_DELIVERY_COMPLETE -> _root.onDeliveryComplete(obj as ByteArray)
                    }
                }
            }
        }

        private object Helper {
            val INSTANCE: PublishDeliveryMonitor by lazy { PublishDeliveryMonitor() }
        }

        @JvmStatic
        internal fun get(): PublishDeliveryMonitor = Helper.INSTANCE
    }
}
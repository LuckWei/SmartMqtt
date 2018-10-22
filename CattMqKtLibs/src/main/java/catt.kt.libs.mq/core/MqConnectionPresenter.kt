package catt.kt.libs.mq.core

import android.content.Context
import android.os.*
import android.util.Log.e
import android.util.Log.i
import catt.kt.libs.mq.listeners.*
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

internal class MqConnectionPresenter constructor(_context: Context) :
    MqBase(_context), IMqttActionListener, IConnectionPresenter {
    private val _TAG: String = MqConnectionPresenter::class.java.simpleName
    private val _handlerWeakR: WeakReference<PresenterHandler> by lazy { WeakReference(PresenterHandler(this@MqConnectionPresenter)) }
    private val _subscribeMessagesMonitor: SubscribeMessagesMonitor by lazy { SubscribeMessagesMonitor.get() }
    private val _publishDeliveryMonitor: PublishDeliveryMonitor by lazy { PublishDeliveryMonitor.get() }
    private val _connectionMonitor: ConnectionMonitor by lazy { ConnectionMonitor.get() }

    override fun traceDebug(tag: String?, message: String?) {
        client ?: return
    }

    override fun traceException(tag: String?, message: String?, e: Exception?) {
        client ?: return
    }

    override fun traceError(tag: String?, message: String?) {
        client ?: return
    }

    override fun onSuccess(token: IMqttToken?) {
        client ?: return
        token ?: return
        acquireWakeLock()
        val operations: MqOperations = userContext2MqOperations(token)
        i(_TAG, "Mq Operation[$operations] -> Completed.")
        when (operations) {
            MqOperations.CONNECT -> client!!.setBufferOpts(disOptions)
        }
        releaseWakeLock()
    }

    override fun onFailure(token: IMqttToken?, ex: Throwable?) {
        client ?: return
        token ?: return
        acquireWakeLock()
        val operations: MqOperations = userContext2MqOperations(token)
        e(_TAG, "Mq Operation[$operations] -> Failed.", ex)
        when (operations) {
            MqOperations.CONNECT -> {
                removeMessages(CODE_RECONNECT)
                sendEmptyMessage(CODE_RECONNECT, TimeUnit.SECONDS.toMillis(3))
            }
        }
        releaseWakeLock()
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        client ?: return
        serverURI ?: return
        _connectionMonitor.onConnectCompleteOfMessage(reconnect, serverURI)
    }

    override fun connectionLost(cause: Throwable?) {
        client ?: return
        cause ?: return
        _connectionMonitor.onConnectionLostOfMessage(cause)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        client ?: return
        topic ?: return
        message ?: return
        acquireWakeLock()
        _subscribeMessagesMonitor.onSubscribeMessageOfMessage(
            topic, message.id, message.payload, message.qos, message.isDuplicate, message.isRetained
        )
        releaseWakeLock()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        client ?: return
        token?.apply {
            _publishDeliveryMonitor.onDeliveryCompleteOfMessage(token.message.payload)
        }
    }

    override fun isConnected(): Boolean = client.run { this?.isConnected ?: return@run false }

    override fun connect() {
        client ?: return
        _connectionMonitor.onConnectingOfMessage(client!!.serverURI)
        connect(this)
    }

    override fun disconnect(quiesceTimeout: Long) = disconnect(quiesceTimeout, this)

    override fun publishMessage(topic: String, message: String) =
        publishMessage(topic, message.toByteArray(Charsets.UTF_8))

    private fun publishMessage(topic: String, payload: ByteArray) = publish(topic, payload, this)

    override fun subscribe(topics: Array<String>) = subscribe(topics, this)

    private fun sendEmptyMessage(what: Int, delayMillis: Long) {
        _handlerWeakR.get()?.apply {
            sendEmptyMessageDelayed(what, delayMillis)
        }
    }

    private fun obtainMessage(what: Int, obj: Any) {
        _handlerWeakR.get()?.apply {
            obtainMessage(what, obj).sendToTarget()
        }
    }

    private fun removeMessages(what: Int) {
        _handlerWeakR.get()?.apply {
            removeMessages(what)
        }
    }

    private fun removeCallbacksAndMessages() {
        _handlerWeakR.get()?.apply {
            removeCallbacksAndMessages(null)
            _handlerWeakR.clear()
        }
    }

    override fun destroyOwn() {
        e(_TAG, "### Begin destroy the MQ.client!!!")
        try {
            removeCallbacksAndMessages()
            disconnect()
        } finally {
            client = null
        }
    }


    private companion object PH {
        private const val CODE_RECONNECT: Int = 10000

        private class PresenterHandler(private val presenter: MqConnectionPresenter) :
            Handler(
                HandlerThread(":MQ_CONNECTION_BACKGROUND_THREAD", Process.THREAD_PRIORITY_BACKGROUND)
                    .apply { start() }.looper
            ) {
            override fun handleMessage(msg: Message?) {
                presenter.client ?: return
                msg?.apply {
                    when (what) {
                        CODE_RECONNECT -> if (!presenter.isConnected()) presenter.connect()
                    }
                }
            }
        }
    }
}
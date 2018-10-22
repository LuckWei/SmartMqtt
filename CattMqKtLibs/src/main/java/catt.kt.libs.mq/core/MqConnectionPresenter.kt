package catt.kt.libs.mq.core

import android.accounts.NetworkErrorException
import android.content.Context
import android.os.*
import android.util.Log.*
import catt.kt.libs.mq.listeners.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.TimeUnit

internal class MqConnectionPresenter constructor(_context: Context) :
    BaseMQ(_context), IMqttActionListener, IConnectionPresenter {
    private val _TAG: String = MqConnectionPresenter::class.java.simpleName
    private var _handler: PresenterHandler? = PresenterHandler(this@MqConnectionPresenter)
    private val _subscribeMessagesMonitor: SubscribeMessagesMonitor by lazy { SubscribeMessagesMonitor.get() }
    private val _publishDeliveryMonitor: PublishDeliveryMonitor by lazy { PublishDeliveryMonitor.get() }
    private val _connectionMonitor: ConnectionMonitor by lazy { ConnectionMonitor.get() }

    override fun onSuccess(token: IMqttToken?) {
        token ?: return
        acquireWakeLock()
        val operations: MqOperations = userContext2MqOperations(token)
        i(_TAG, "Mq Operation[$operations] -> Completed.")
        when (operations) {
            MqOperations.CONNECT -> {
                (token.client as MqttAndroidClient).setBufferOpts(disOptions)
            }
            MqOperations.DISCONNECT -> {
                i(_TAG, "Mq Operation[$operations]: Begin MQ.client.close()")
                token.client.close()
            }
        }
        releaseWakeLock()
    }

    override fun onFailure(token: IMqttToken?, ex: Throwable?) {
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
        serverURI ?: return
        _connectionMonitor.onConnectCompleteOfMessage(reconnect, serverURI)
    }

    override fun connectionLost(cause: Throwable?) {
        cause ?: return
        _connectionMonitor.onConnectionLostOfMessage(cause)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        topic ?: return
        message ?: return
        acquireWakeLock()
        _subscribeMessagesMonitor.onSubscribeMessageOfMessage(
            topic, message.id, message.payload, message.qos, message.isDuplicate, message.isRetained
        )
        releaseWakeLock()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        token?.apply {
            _publishDeliveryMonitor.onDeliveryCompleteOfMessage(token.message.payload)
        }
    }

    override fun isConnected(): Boolean = client.isConnected

    override fun connect() {
        _connectionMonitor.onConnectingOfMessage(client.serverURI)
        if (isConnectedNetwork) {
            connect(this)
        } else {
            w(_TAG, "@ Please check the network...")
            _connectionMonitor.onConnectionLostOfMessage(NetworkErrorException("Please check the network"))
            removeMessages(CODE_RECONNECT)
            sendEmptyMessage(CODE_RECONNECT, TimeUnit.SECONDS.toMillis(3))
        }
    }

    override fun disconnect(quiesceTimeout: Long) = disconnect(quiesceTimeout, this)

    override fun publishMessage(topic: String, message: String) =
        publishMessage(topic, message.toByteArray(Charsets.UTF_8))

    private fun publishMessage(topic: String, payload: ByteArray) = publish(topic, payload, this)

    override fun subscribe(topics: Array<String>) = subscribe(topics, this)

    private fun sendEmptyMessage(what: Int, delayMillis: Long) {
        _handler?.apply {
            sendEmptyMessageDelayed(what, delayMillis)
        }
    }

    private fun obtainMessage(what: Int, obj: Any) {
        _handler?.apply {
            obtainMessage(what, obj).sendToTarget()
        }
    }

    private fun removeMessages(what: Int) {
        _handler?.apply {
            removeMessages(what)
        }
    }

    private fun removeCallbacksAndMessages() {
        _handler?.apply {
            removeCallbacksAndMessages(null)
        }
        _handler = null
    }

    override fun destroyOwn() {
        removeCallbacksAndMessages()
        disconnect()
    }

    private companion object PH {
        private const val CODE_RECONNECT: Int = 10000

        private class PresenterHandler(private val presenter: MqConnectionPresenter) :
            Handler(
                HandlerThread(":MQ_CONNECTION_BACKGROUND_THREAD", Process.THREAD_PRIORITY_BACKGROUND)
                    .apply { start() }.looper
            ) {
            override fun handleMessage(msg: Message?) {
                msg?.apply {
                    when (what) {
                        CODE_RECONNECT -> if (!presenter.isConnected()) presenter.connect()
                    }
                }
            }
        }
    }
}
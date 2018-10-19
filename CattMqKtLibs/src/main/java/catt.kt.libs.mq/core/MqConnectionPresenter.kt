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
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

internal class MqConnectionPresenter constructor(_context: Context) :
    MqBasePresenter(_context.applicationContext), IConnectionPresenter {

    private val _TAG: String = MqConnectionPresenter::class.java.simpleName
    private val _handlerWeakR: WeakReference<PresenterHandler> by lazy { WeakReference(PresenterHandler(this@MqConnectionPresenter)) }
    private val _subscribeMessagesMonitor: SubscribeMessagesMonitor by lazy { SubscribeMessagesMonitor.get() }
    private val _publishDeliveryMonitor: PublishDeliveryMonitor by lazy { PublishDeliveryMonitor.get() }
    private val _connectionMonitor: ConnectionMonitor by lazy { ConnectionMonitor.get() }

    private var mqActionListenerWeakR: WeakReference<IMqttActionListener>? = null

    init {
        setOnMqttActionListener { completed, token, ex ->
            token ?: return@setOnMqttActionListener
            val operations: MqOperations = userContext2MqOperations(token)
            when {
                completed -> i(_TAG, "Mq Operation[$operations] -> Completed.")
                !completed && operations === MqOperations.CONNECT -> {
                    e(_TAG, "Mq Operation[$operations] -> Failed.", ex)
                    sendEmptyMessage(_CODE_RECONNECT, TimeUnit.SECONDS.toMillis(3))
                }
                else -> e(_TAG, "Mq Operation[$operations] -> Failed.", ex)
            }
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        super.connectComplete(reconnect, serverURI)
        serverURI ?: return
        _connectionMonitor.onConnectCompleteOfMessage(reconnect, serverURI)
    }

    override fun connectionLost(cause: Throwable?) {
        super.connectionLost(cause)
        cause ?: return
        _connectionMonitor.onConnectionLostOfMessage(cause)
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        super.messageArrived(topic, message)
        topic ?: return
        message ?: return
        acquireWakeLock()
        _subscribeMessagesMonitor.onSubscribeMessageOfMessage(
            topic, message.id, message.payload, message.qos, message.isDuplicate, message.isRetained
        )
        releaseWakeLock()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        super.deliveryComplete(token)
        token?.apply {
            _publishDeliveryMonitor.onDeliveryCompleteOfMessage(token.message.payload)
        }
    }

    private fun setOnMqttActionListener(listener: IMqttActionListener.(completed: Boolean, token: IMqttToken?, ex: Throwable?) -> Unit) {
        mqActionListenerWeakR = WeakReference(object : IMqttActionListener {
            override fun onSuccess(token: IMqttToken?) {
                acquireWakeLock()
                listener(true, token, null)
                releaseWakeLock()
            }

            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                acquireWakeLock()
                listener(false, token, ex)
                releaseWakeLock()
            }
        })
    }

    override fun isConnected(): Boolean = mqttAndroidClient.isConnected

    override fun connect() = when (mqActionListenerWeakR != null && mqActionListenerWeakR!!.get() != null) {
        true -> {
            _connectionMonitor.onConnectingOfMessage(mqttAndroidClient.serverURI)
            connect(mqActionListenerWeakR!!.get())
        }
        false -> {
            _connectionMonitor.onConnectingOfMessage(mqttAndroidClient.serverURI)
            connect(null)
        }
    }

    override fun disconnect(quiesceTimeout: Long) =
        when (mqActionListenerWeakR != null && mqActionListenerWeakR!!.get() != null) {
            true -> disconnect(quiesceTimeout, mqActionListenerWeakR!!.get())
            false -> disconnect(quiesceTimeout, null)
        }

    override fun publishMessage(topic: String, message: String) =
        publishMessage(topic, message.toByteArray(Charsets.UTF_8))

    private fun publishMessage(topic: String, payload: ByteArray) =
        when (mqActionListenerWeakR != null && mqActionListenerWeakR!!.get() != null) {
            true -> publish(topic, payload, mqActionListenerWeakR!!.get())
            false -> publish(topic, payload, null)
        }

    override fun subscribe(topics: Array<String>) =
        when (mqActionListenerWeakR != null && mqActionListenerWeakR!!.get() != null) {
            true -> subscribe(topics, mqActionListenerWeakR!!.get())
            false -> subscribe(topics, null)
        }

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
        mqActionListenerWeakR?.clear()
        disconnect()
        removeCallbacksAndMessages()
    }


    companion object MqMessageCode {
        private const val _CODE_RECONNECT: Int = 10000

        private val _looper: Looper by lazy {
            HandlerThread(":MQ_CONNECTION_BACKGROUND_THREAD", Process.THREAD_PRIORITY_BACKGROUND).apply {
                start()
            }.looper
        }

        private class PresenterHandler(private val presenter: IConnectionPresenter) : Handler(_looper) {
            override fun handleMessage(msg: Message?) {
                msg?.apply {
                    when (what) {
                        _CODE_RECONNECT -> if (!presenter.isConnected()) presenter.connect()
                    }
                }
            }
        }
    }
}
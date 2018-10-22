package catt.kt.libs.mq.core

import android.app.Service
import android.content.Context
import android.os.PowerManager
import android.util.Log.*
import catt.kt.libs.mq.MqConfigure


import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttTraceHandler
import org.eclipse.paho.client.mqttv3.*
import java.lang.ref.WeakReference

internal abstract class MqBase(private val _context: Context) : MqttCallbackExtended, MqttTraceHandler {
    private val _TAG: String = MqBase::class.java.simpleName
    private var wakelock: PowerManager.WakeLock? = null
    internal var client: WeakReference<MqttAndroidClient>? = null

    init {
        i(_TAG, "@ Initialize M.Q.T.T client. ${toString()}")
        client = WeakReference(
            MqttAndroidClient(
                _context,
                MqConfigure.serverUrl,
                MqConfigure.clientId,
                MemoryPersistence(),
                MqttAndroidClient.Ack.AUTO_ACK
            )
        )
        client?.get()?.apply {
            registerResources(_context)
            setTraceEnabled(MqConfigure.isTrace)
            setCallback(this@MqBase)
            setTraceCallback(this@MqBase)
        }
    }

    val options: MqttConnectOptions by lazy {
        MqttConnectOptions().apply {
            userName = MqConfigure.userName
            password = MqConfigure.password.toCharArray()
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
            isAutomaticReconnect = true
            isCleanSession = MqConfigure.isCleanSession
            keepAliveInterval = MqConfigure.keepAliveInterval
            connectionTimeout = MqConfigure.connectionTimeout
            maxInflight = MqConfigure.maxInflight
        }
    }

    val disOptions: DisconnectedBufferOptions by lazy {
        DisconnectedBufferOptions().apply {
            isBufferEnabled = true
            bufferSize = MqConfigure.bufferSize
            isDeleteOldestMessages = true
            isPersistBuffer = true
        }
    }

    fun connect(callback: IMqttActionListener?) {
        i(_TAG, "@ Start trying to connect...")
        try {
            client?.get()?.apply {
                if (isConnected) return
                connect(options, MqOperations.CONNECT, callback)
            }
        } catch (ex: Exception) {
            e(_TAG, "Unable to connect.", ex)
        }
    }

    fun disconnect(quiesceTimeout: Long = 1000L * 10, callback: IMqttActionListener?) {
        i(_TAG, "@ Start trying to disconnect...")
        try {
            client?.get()?.apply {
                disconnect(quiesceTimeout, MqOperations.DISCONNECT, callback)
            }
        } catch (ex: Exception) {
            e(_TAG, "Unable to disconnect.", ex)
        } finally {
            releaseWakeLock()
        }
    }

    fun publish(topic: String, payload: ByteArray, callback: IMqttActionListener?) {
        i(_TAG, "@ Start trying to publish...")
        try {
            client?.get()?.apply {
                if (!isConnected) return
                publish(topic, payload, MqConfigure.qos, true, MqOperations.PUBLISH, callback)
            }
        } catch (ex: Exception) {
            e(_TAG, "Unable to publish.", ex)
        }
    }

    fun subscribe(topic: Array<String>, callback: IMqttActionListener?) {
        i(_TAG, "@ Start trying to subscribe...")
        try {
            client?.get()?.apply {
                if (!isConnected) return
                subscribe(topic, getQos(topic.size), MqOperations.SUBSCRIBE, callback)
            }
        } catch (ex: MqttException) {
            e(_TAG, "Unable to subscribe.", ex)
        }
    }

    fun unsubscribe(topic: Array<String>, callback: IMqttActionListener?) {
        i(_TAG, "@ Start trying to unsubscribe...")
        try {
            client?.get()?.apply {
                if (!isConnected) return
                unsubscribe(topic, MqOperations.UNSUBSCRIBE, callback)
            }
        } catch (ex: Exception) {
            e(_TAG, "Unable to unsubscribe.", ex)
        }
    }

    private fun getQos(length: Int): IntArray {
        val qos = IntArray(length)
        for (i: Int in qos.indices) {
            qos[i] = MqConfigure.qos
        }
        return qos
    }


    fun releaseWakeLock() {
        if (wakelock != null && wakelock!!.isHeld) {
            wakelock!!.release()
        }
    }

    fun acquireWakeLock() {
        if (wakelock == null) {
            val pm = _context.getSystemService(Service.POWER_SERVICE) as PowerManager
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, toString())
        }
        wakelock ?: return
        wakelock!!.acquire()
    }


    override fun toString(): String {
        return "serverUrl = ${MqConfigure.serverUrl}, " +
                "clientId = ${MqConfigure.clientId}, " +
                "qos = ${MqConfigure.qos}, " +
                "isCleanSession = ${MqConfigure.isCleanSession}"
    }

    companion object {
        @JvmStatic
        fun userContext2MqOperations(token: IMqttToken): MqOperations = token.userContext as MqOperations
    }

}

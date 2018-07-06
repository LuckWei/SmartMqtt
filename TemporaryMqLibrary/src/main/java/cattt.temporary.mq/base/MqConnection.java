package cattt.temporary.mq.base;

import android.app.Service;
import android.os.PowerManager;

import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.base.model.IMqConnectionable;
import cattt.temporary.mq.logger.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.eclipse.paho.android.service.MqttAndroidClient.Ack.AUTO_ACK;

public class MqConnection implements IMqConnectionable {
    private static Log logger = Log.getLogger(MqConnection.class);
    private PowerManager.WakeLock wakelock;
    private String wakeLockTag;

    private MqService mService;
    private MqttAndroidClient mClient;

    protected MqConnection(MqService service) {
        this.mService = service;
        mClient = getMqttAndroidClient();
        mClient.registerResources(service);
        wakeLockTag = new StringBuilder(this.getClass().getSimpleName())
                .append("ClientId=").append(mClient.getClientId())
                .append("ServerUri=").append(mClient.getServerURI()).toString();
        mClient.setCallback(mService);
    }

    public boolean isConnected() {
        return mClient != null && mClient.isConnected();
    }

    @Override
    public void connect() {
        logger.i("connect");
        try {
            if (!isConnected()) {
                mClient.connect(getMqttConnectOptions(), MqOperations.CONNECT, mService);
            }
        } catch (Exception ex) {
            logger.e("Unable to connect.", ex);
        }
    }

    @Override
    public void disconnect(long quiesceTimeout) {
        try {
            if (isConnected()) {
                mClient.disconnect(quiesceTimeout, MqOperations.DISCONNECT, mService);
            }
        } catch (Exception ex) {
            logger.e("Unable to disconnected.", ex);
        } finally {
            releaseWakeLock();
        }
    }

    @Override
    public void subscribe(String[] topic) {
        if (!isConnected()) {
            return;
        }
        checkTopicArray(topic);
        try {
            mClient.subscribe(topic, getQos(topic.length), MqOperations.SUBSCRIBE, mService);
        } catch (MqttException ex) {
            logger.e("Unable to subscribe.", ex);
        }
    }

    @Override
    public void unsubscribe(String[] topic) {
        if (!isConnected()) {
            return;
        }
        checkTopicArray(topic);
        try {
            mClient.unsubscribe(topic, MqOperations.UNSUBSCRIBE, mService);
        } catch (Exception ex) {
            logger.e("Unable to unsubscribe.", ex);
        }
    }

    private MqttConnectOptions getMqttConnectOptions() {
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(MqConfigure.userName);
        options.setPassword(MqConfigure.password.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setKeepAliveInterval(10);
        options.setConnectionTimeout(30);
        options.setMaxInflight(100);
        return options;
    }

    private MemoryPersistence getMemoryPersistence() {
        final MemoryPersistence persistence = new MemoryPersistence();
        return persistence;
    }

    private MqttAndroidClient getMqttAndroidClient() {
        return new MqttAndroidClient(
                mService.getApplicationContext(),
                MqConfigure.serverUri,
                MqConfigure.clientId,
                getMemoryPersistence(),
                AUTO_ACK);
    }

    /**
     * Acquires a partial wake lock for this client
     */
    public void acquireWakeLock() {
        if (wakelock == null) {
            PowerManager pm = (PowerManager) mService.getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
        }
        wakelock.acquire();
    }

    /**
     * Releases the currently held wake lock for this client
     */
    public void releaseWakeLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }

    private void checkTopicArray(String[] topic) {
        if (topic == null) {
            throw new NullPointerException("Topics Cannot be null.");
        }

        if (topic.length <= 0) {
            throw new IllegalArgumentException("Topics size = 0.");
        }
    }

    private int[] getQos(int length) {
        final int[] qos = new int[length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 2;
        }
        return qos;
    }
}

package cattt.temporary.mq.base;

import android.text.TextUtils;

import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.base.model.IMqConnectionAble;
import cattt.temporary.mq.logger.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;

import static org.eclipse.paho.android.service.MqttAndroidClient.Ack.AUTO_ACK;

public class MqConnection implements IMqConnectionAble {
    private static Log logger = Log.getLogger(MqConnection.class);

    private String wakeLockTag;

    private MqService mService;
    private MqttAndroidClient mClient;

    protected MqConnection(MqService service) {
        this.mService = service;
        mClient = getMqttAndroidClient();
        mClient.setTraceEnabled(MqConfigure.isTrace);
        mClient.setTraceCallback(mService);
        wakeLockTag = new StringBuilder(this.getClass().getSimpleName())
                .append("ClientId=").append(mClient.getClientId())
                .append("ServerUri=").append(mClient.getServerURI()).toString();
        mClient.setCallback(mService);
    }


    @Override
    public MqttAndroidClient getMqClient() {
        return mClient;
    }

    @Override
    public String getWakeLockTag() {
        return wakeLockTag;
    }

    @Override
    public boolean isConnected() {
        return mClient != null && mClient.isConnected();
    }

    @Override
    public void connect() {
        logger.i("connect");
        try {
            if (!isConnected()) {
                mClient.connect(getMqttConnectOptions(), MqOperations.CONNECT, mService.mConnectListener);
                mClient.registerResources(mService);
            }
        } catch (Exception ex) {
            logger.e("Unable to connect.", ex);
        }
    }

    @Override
    public void disconnect(long quiesceTimeout) {
        try {
            if (isConnected()) {
                mClient.unregisterResources();
                mClient.disconnect(quiesceTimeout, MqOperations.DISCONNECT, mService.mDisconnectListener);
                mClient = null;
            }
        } catch (Exception ex) {
            logger.e("Unable to disconnected.", ex);
        } finally {
            mService.releaseWakeLock();
        }
    }

    @Override
    public void subscribe(String[] topic) {
        if (!isConnected()) {
            return;
        }
        checkTopic(topic);
        try {
            mClient.subscribe(topic, getQos(topic.length), MqOperations.SUBSCRIBE, mService.mSubscribeListener);
        } catch (MqttException ex) {
            logger.e("Unable to subscribe.", ex);
        }
    }

    @Override
    public void publish(String topic, String message) {
        if (!isConnected()) {
            return;
        }
        checkTopic(topic);
        try {
            mClient.publish(topic, toUtf8(message).getBytes(), getQos(), true, MqOperations.PUBLISH, mService.mPublishListener);
        } catch (Exception ex) {
            logger.e("Unable to publish.", ex);
        }
    }

    @Override
    public void unsubscribe(String[] topic) {
        if (!isConnected()) {
            return;
        }
        checkTopic(topic);
        try {
            mClient.unsubscribe(topic, MqOperations.UNSUBSCRIBE, mService.mUnsubscribeListener);
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
        options.setKeepAliveInterval(MqConfigure.keepAliveInterval);
        options.setConnectionTimeout(MqConfigure.connectionTimeout);
        options.setMaxInflight(MqConfigure.maxInflight);
        return options;
    }

    @Override
    public DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions options = new DisconnectedBufferOptions();
        options.setBufferEnabled(true);
        options.setBufferSize(MqConfigure.bufferSize);
        options.setDeleteOldestMessages(true);
        options.setPersistBuffer(true);
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


    private void checkTopic(String[] topic) {
        if (topic == null) {
            throw new NullPointerException("Topics Cannot be null.");
        }

        if (topic.length <= 0) {
            throw new IllegalArgumentException("Topics size = 0.");
        }
    }

    private void checkTopic(String topic) {
        if (TextUtils.isEmpty(topic)) {
            throw new NullPointerException("Topic Cannot be null.");
        }
    }

    private int[] getQos(int length) {
        final int[] qos = new int[length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = getQos();
        }
        return qos;
    }

    private String toUtf8(String message) throws UnsupportedEncodingException {
        return new String(message.getBytes("UTF-8"),"UTF-8");
    }

    @Override
    public int getQos() {
        return MqConfigure.qos;
    }
}

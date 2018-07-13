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


    public MqttAndroidClient getMqClient(){
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
                mClient.registerResources(mService);
                mClient.connect(getMqttConnectOptions(), MqOperations.CONNECT, mService.mConnectListener);
            }
        } catch (Exception ex) {
            logger.e("Unable to connect.", ex);
        }
    }

    @Override
    public void disconnect(long quiesceTimeout) {
        try {
            if (isConnected()) {
                mClient.disconnect(quiesceTimeout, MqOperations.DISCONNECT, mService.mDisconnectListener);
            }
        } catch (Exception ex) {
            logger.e("Unable to disconnected.", ex);
        } finally {
            mClient.unregisterResources();
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
            mClient.publish(topic, message.getBytes(), getQos(), true, MqOperations.PUBLISH, mService.mPublishListener);
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
        options.setKeepAliveInterval(10);
        options.setConnectionTimeout(30);
        options.setMaxInflight(100);
        return options;
    }

    @Override
    public DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions options = new DisconnectedBufferOptions();
        options.setBufferEnabled(true);
        options.setBufferSize(5000);
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

    @Override
    public int getQos() {
        return MqConfigure.qos;
    }
}

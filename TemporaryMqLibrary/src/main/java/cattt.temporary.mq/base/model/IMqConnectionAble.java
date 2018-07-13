package cattt.temporary.mq.base.model;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;

public interface IMqConnectionAble {
    String getWakeLockTag();

    int getQos();

    void connect();

    void disconnect(long quiesceTimeout);

    void subscribe(String[] topic);

    void unsubscribe(String[] topic);

    void publish(String topic, String message);

    boolean isConnected();

    MqttAndroidClient getMqClient();

    DisconnectedBufferOptions getDisconnectedBufferOptions();
}

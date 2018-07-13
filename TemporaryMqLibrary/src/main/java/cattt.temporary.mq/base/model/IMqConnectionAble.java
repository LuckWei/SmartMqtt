package cattt.temporary.mq.base.model;


public interface IMqConnectionAble {
    String getWakeLockTag();

    int getQos();

    void connect();

    void disconnect(long quiesceTimeout);

    void subscribe(String[] topic);

    void unsubscribe(String[] topic);

    void publish(String topic, String message);

    boolean isConnected();
}

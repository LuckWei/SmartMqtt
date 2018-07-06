package cattt.temporary.mq.base.model;


public interface IMqConnectionable {

    void connect();

    void disconnect(long quiesceTimeout);

    void subscribe(String[] topic);

    void unsubscribe(String[] topic);
}

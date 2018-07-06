package cattt.temporary.mq.callback;

public interface OnMqConnectionListener {
    void onConnected(String serverUri);
    void onDisconnection(Throwable ex);
}

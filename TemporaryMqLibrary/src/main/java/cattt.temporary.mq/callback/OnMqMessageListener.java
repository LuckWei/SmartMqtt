package cattt.temporary.mq.callback;



public interface OnMqMessageListener {
    void onMessageArrived(String topic, String message);
}

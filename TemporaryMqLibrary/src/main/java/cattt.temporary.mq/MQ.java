package cattt.temporary.mq;

import android.content.Context;

import cattt.temporary.mq.base.ServiceConnectionException;
import cattt.temporary.mq.callback.OnConnectionListener;
import cattt.temporary.mq.wrapper.MqServiceConnection;

public class MQ {

    public static void bindService(Context context, OnConnectionListener listener) {
        MqServiceConnection.get().addOnConnectionListener(listener).init(context.getApplicationContext()).bindServiceOfMessage();
    }

    public static boolean isConnected() {
        return MqServiceConnection.get().isConnected();
    }

    public static void publishMessage(String topic, String message) throws ServiceConnectionException {
        MqServiceConnection.get().publishMessage(topic, message);
    }

    public static void subscribe(String[] topic) throws ServiceConnectionException {
        MqServiceConnection.get().subscribe(topic);
    }

    public static void unbindService() throws IllegalArgumentException {
        MqServiceConnection.get().unbindService();
    }
}

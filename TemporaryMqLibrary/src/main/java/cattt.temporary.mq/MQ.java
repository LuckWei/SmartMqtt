package cattt.temporary.mq;

import android.content.Context;

import cattt.temporary.mq.base.ServiceConnectionException;
import cattt.temporary.mq.callback.OnConnectionListener;

public class MQ {

    public static void bindService(Context context, OnConnectionListener listener){
        BridgeConnection.get().addOnConnectionListener(listener).init(context.getApplicationContext()).bindServiceOfMessage();
    }

    public static boolean isConnected() throws ServiceConnectionException {
        return BridgeConnection.get().isConnected();
    }

    public static void publishMessage(String topic, String message) throws ServiceConnectionException {
        BridgeConnection.get().publishMessage(topic, message);
    }

    public static void unbindService() throws IllegalArgumentException {
        BridgeConnection.get().unbindService();
    }
}

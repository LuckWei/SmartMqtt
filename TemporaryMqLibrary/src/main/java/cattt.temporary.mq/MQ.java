package cattt.temporary.mq;

import android.content.Context;

import cattt.temporary.mq.callback.OnConnectionListener;

public class MQ {

    public static void bindService(Context context, OnConnectionListener listener){
        BridgeConnection.get().addOnConnectionListener(listener).init(context.getApplicationContext()).bindServiceOfMessage();
    }

    public static void unbindService() throws IllegalArgumentException{
        BridgeConnection.get().unbindService();
    }
}

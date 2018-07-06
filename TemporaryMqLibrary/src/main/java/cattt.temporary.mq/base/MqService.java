package cattt.temporary.mq.base;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.MqMessageMonitor;
import cattt.temporary.mq.MqStateMonitor;
import cattt.temporary.mq.logger.Log;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.TimeUnit;

public class MqService extends Service implements MqttCallbackExtended, IMqttActionListener {
    private static final int MSG_CODE_RECONNECT = 10000;
    public static final String ACTION = "com.hcb.phmq.base.MqService.ACTION_CONNECTION_MQTT";
    public static final String CATEGORY = "GlOy2CInGKY0PmZg785wzdBbWI5id4BQKgva6G7g3UjEKPkWByPEL7XIPTNHEv5O";
    private static Log logger = Log.getLogger(MqService.class);

    private MqBinder mMqBinder;
    private MqHandler handler;
    private MqConnection mMqConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.e("onCreate()");
        mMqConnection = new MqConnection(this);
        mMqBinder = new MqBinder(this);
        handler = new MqHandler(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        logger.e("onBind()");
        return mMqBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        logger.e("onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        logger.e("onDestroy()");
        mMqConnection.unsubscribe(MqConfigure.topics);
        mMqConnection.disconnect(0);
        mMqConnection = null;
        super.onDestroy();
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        MqStateMonitor.get().onConnectedOfMessage(serverURI);
        mMqConnection.subscribe(MqConfigure.topics);
    }

    @Override
    public void connectionLost(Throwable cause) {
        MqStateMonitor.get().onDisconnectionOfMessage(cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        mMqConnection.acquireWakeLock();
        final byte[] bytes = message.getPayload();
        if (bytes == null) {
            mMqConnection.releaseWakeLock();
            return;
        }
        if (bytes.length <= 0) {
            mMqConnection.releaseWakeLock();
            return;
        }
        MqMessageMonitor.get().onMessageArrivedOfMessage(topic, new String(bytes));
        mMqConnection.releaseWakeLock();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onSuccess(IMqttToken iToken) {
        if (mMqConnection != null) {
            mMqConnection.acquireWakeLock();
            if (iToken.getUserContext() != null && iToken.getUserContext() instanceof MqOperations) {
                MqOperations opt = (MqOperations) iToken.getUserContext();
                logger.i("onSuccess %s", opt);
            }
            mMqConnection.releaseWakeLock();
        }
    }

    @Override
    public void onFailure(final IMqttToken iToken, Throwable ex) {
        if (mMqConnection != null) {
            mMqConnection.acquireWakeLock();
            if (iToken.getUserContext() != null && iToken.getUserContext() instanceof MqOperations) {
                MqOperations opt = (MqOperations) iToken.getUserContext();
                logger.w(String.format("onFailure %s", opt), ex);
                switch (opt) {
                    case CONNECT:
                        handler.sendEmptyMessage(MSG_CODE_RECONNECT);
                        break;
                }
            }
            mMqConnection.releaseWakeLock();
        }
    }

    public void startConnect() {
        mMqConnection.connect();
    }

    private static class MqHandler extends Handler {
        MqService mService;

        public MqHandler(MqService service) {
            this.mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case MSG_CODE_RECONNECT:
                    if (mService != null && mService.mMqConnection != null) {
                        mService.mMqConnection.connect();
                    }
                    break;
            }
        }
    }
}

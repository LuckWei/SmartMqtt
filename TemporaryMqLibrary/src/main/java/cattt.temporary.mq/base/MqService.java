package cattt.temporary.mq.base;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.MqMessageMonitor;
import cattt.temporary.mq.MqStateMonitor;
import cattt.temporary.mq.base.model.IMqConnectionAble;
import cattt.temporary.mq.logger.Log;

import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.TimeUnit;

public class MqService extends Service implements MqttCallbackExtended, MqttTraceHandler {
    private static final int MSG_CODE_RECONNECT = 10000;
    public static final String ACTION = "com.hcb.phmq.base.MqService.ACTION_CONNECTION_MQTT";
    public static final String CATEGORY = "GlOy2CInGKY0PmZg785wzdBbWI5id4BQKgva6G7g3UjEKPkWByPEL7XIPTNHEv5O";
    private static Log logger = Log.getLogger(MqService.class);

    private MqBinder mMqBinder;
    private MqHandler handler;
    private IMqConnectionAble mMqConnection;
    private PowerManager.WakeLock wakelock;

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
        acquireWakeLock();
        final byte[] bytes = message.getPayload();
        if (bytes == null) {
            releaseWakeLock();
            return;
        }
        if (bytes.length <= 0) {
            releaseWakeLock();
            return;
        }
        MqMessageMonitor.get().onMessageArrivedOfMessage(topic, new String(bytes));
        releaseWakeLock();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    protected final IMqttActionListener mDisconnectListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.i("onSuccess %s", opt);
            releaseWakeLock();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable ex) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.w(String.format("onFailure %s", opt), ex);
            releaseWakeLock();
        }
    };

    protected final IMqttActionListener mConnectListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.i("onSuccess %s", opt);
            mMqConnection.getMqClient().setBufferOpts(mMqConnection.getDisconnectedBufferOptions());
            releaseWakeLock();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable ex) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.w(String.format("onFailure %s", opt), ex);
            handler.sendEmptyMessageDelayed(MSG_CODE_RECONNECT, TimeUnit.SECONDS.toMillis(3));
            releaseWakeLock();
        }
    };

    protected final IMqttActionListener mSubscribeListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.i("onSuccess %s", opt);
            releaseWakeLock();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable ex) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.w(String.format("onFailure %s", opt), ex);
            releaseWakeLock();
        }
    };

    protected final IMqttActionListener mUnsubscribeListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.i("onSuccess %s", opt);
            releaseWakeLock();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable ex) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.w(String.format("onFailure %s", opt), ex);
            releaseWakeLock();
        }
    };

    protected final IMqttActionListener mPublishListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken token) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.i("onSuccess %s", opt);
            releaseWakeLock();
        }

        @Override
        public void onFailure(IMqttToken token, Throwable ex) {
            acquireWakeLock();
            MqOperations opt = (MqOperations) token.getUserContext();
            logger.w(String.format("onFailure %s", opt), ex);
            releaseWakeLock();
        }
    };


    /**
     * Releases the currently held wake lock for this client
     */
    public void releaseWakeLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }

    /**
     * Acquires a partial wake lock for this client
     */
    public void acquireWakeLock() {
        if (wakelock == null) {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mMqConnection.getWakeLockTag());
        }
        wakelock.acquire();
    }

    public boolean isConnected(){
        return mMqConnection.isConnected();
    }

    public void startConnect() {
        mMqConnection.connect();
    }

    public void publishMessage(String topic, String message) {
        mMqConnection.publish(topic, message);
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

    @Override
    public void traceDebug(String tag, String message) {
        logger.e("TraceDebug, tag[%s] message >>> %s", tag, message);
    }

    @Override
    public void traceError(String tag, String message) {
        logger.e("TraceError, tag[%s] message >>> %s", tag, message);

    }

    @Override
    public void traceException(String tag, String message, Exception ex) {
        logger.e(String.format("TraceException, tag[%s] message >>> %s", tag, message), ex);
    }
}

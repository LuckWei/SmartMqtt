package cattt.temporary.mq;

import android.content.ComponentName;
import android.os.IBinder;

import cattt.temporary.mq.base.BaseServiceConnection;
import cattt.temporary.mq.base.MqBinder;
import cattt.temporary.mq.base.MqService;
import cattt.temporary.mq.base.ServiceConnectionException;
import cattt.temporary.mq.callback.OnConnectionListener;
import cattt.temporary.mq.logger.Log;

public class BridgeConnection extends BaseServiceConnection {
    private static Log logger = Log.getLogger(BridgeConnection.class);

    private OnConnectionListener mListener;

    public BridgeConnection addOnConnectionListener(OnConnectionListener listener) {
        if (listener == null) {
            new NullPointerException("param cannot be null.");
        }
        this.mListener = listener;
        return this;
    }


    protected BridgeConnection() {
    }

    @Override
    protected String getConnectionType() {
        return BridgeConnection.class.getSimpleName();
    }

    @Override
    protected String getImplicitAction() {
        return MqService.ACTION;
    }

    @Override
    protected String getOwnCategory() {
        return MqService.CATEGORY;
    }

    private void startConnect() throws ServiceConnectionException {
        if (getBinder() == null) {
            throw new ServiceConnectionException("Service is not connected");
        }
        ((MqBinder) getBinder()).startConnect();
    }

    public boolean isConnected() throws ServiceConnectionException {
        if (getBinder() == null) {
            throw new ServiceConnectionException("Service is not connected");
        }
        return ((MqBinder) getBinder()).isConnected();
    }

    public void publishMessage(String topic, String message) throws ServiceConnectionException {
        if (getBinder() == null) {
            throw new ServiceConnectionException("Service is not connected");
        }
        ((MqBinder) getBinder()).publishMessage(topic, message);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        startConnect();
        if (mListener != null) {
            mListener.onConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
        if (mListener != null) {
            mListener.onDisconnected();
        }
    }

    private static final class Helper {
        private static final BridgeConnection INSTANCE = new BridgeConnection();
    }

    protected static BridgeConnection get() {
        return Helper.INSTANCE;
    }
}

package cattt.temporary.mq;

import android.os.Handler;
import android.os.Message;

import cattt.temporary.mq.callback.OnMqConnectionListener;

import java.util.ArrayList;

public class MqStateMonitor {
    private static final int MSG_CODE_CONNECTED = 10000;
    private static final int MSG_CODE_DISCONNECTION = 10001;

    private MainHandler handler;

    private MqStateMonitor() {
        handler = new MainHandler(this);
    }

    private ArrayList<OnMqConnectionListener> mListeners = new ArrayList<>();


    public void addOnMqConnectionListener(OnMqConnectionListener listener) throws NullPointerException {
        if (listener == null) {
            throw new NullPointerException("listener cannot null.");
        }
        mListeners.add(listener);
    }

    public void removeOnMqConnectionListener(OnMqConnectionListener listener) throws NullPointerException {
        if (listener == null) {
            throw new NullPointerException("listener cannot null.");
        }
        mListeners.remove(listener);
    }

    public void onConnectedOfMessage(String serverUri) {
        handler.obtainMessage(MSG_CODE_CONNECTED, serverUri).sendToTarget();
    }

    public void onDisconnectionOfMessage(Throwable ex) {
        handler.obtainMessage(MSG_CODE_DISCONNECTION, ex).sendToTarget();
    }

    private void onConnected(String serverUri) {
        for (OnMqConnectionListener listener : mListeners) {
            listener.onConnected(serverUri);
        }
    }

    private void onDisconnection(Throwable ex) {
        for (OnMqConnectionListener listener : mListeners) {
            listener.onDisconnection(ex);
        }
    }

    private static final class Helper {
        private static final MqStateMonitor INSTANCE = new MqStateMonitor();
    }

    public static MqStateMonitor get() {
        return Helper.INSTANCE;
    }

    private static class MainHandler extends Handler {
        private MqStateMonitor monitor;

        public MainHandler(MqStateMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case MSG_CODE_CONNECTED:
                    monitor.onConnected((String) msg.obj);
                    break;
                case MSG_CODE_DISCONNECTION:
                    monitor.onDisconnection((Throwable) msg.obj);
                    break;
            }
        }
    }
}

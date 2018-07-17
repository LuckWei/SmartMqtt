package cattt.temporary.mq.wrapper;

import android.os.Handler;
import android.os.Message;

import cattt.temporary.mq.callback.OnMqMessageListener;

import java.util.ArrayList;

public class MqMessageMonitor {
    private static final int MSG_CODE_MESSAGE_ARRIVED = 10000;

    private MainHandler handler;

    private MqMessageMonitor() {
        handler = new MainHandler(this);
    }

    private ArrayList<OnMqMessageListener> mListeners = new ArrayList<>();


    public void addOnMqMessageListener(OnMqMessageListener listener) throws NullPointerException {
        if (listener == null) {
            throw new NullPointerException("listener cannot null.");
        }
        mListeners.add(listener);
    }

    public void removeOnMqMessageListener(OnMqMessageListener listener) throws NullPointerException {
        if (listener == null) {
            throw new NullPointerException("listener cannot null.");
        }
        mListeners.remove(listener);
    }

    public void handlerOnMessageArrived(String topic, String message) {
        handler.obtainMessage(MSG_CODE_MESSAGE_ARRIVED, new Content(topic, message)).sendToTarget();
    }

    private void onMessageArrived(String topic, String message) {
        for (OnMqMessageListener listener : mListeners) {
            listener.onMessageArrived(topic, message);
        }
    }

    private static final class Helper {
        private static final MqMessageMonitor INSTANCE = new MqMessageMonitor();
    }

    public static MqMessageMonitor get() {
        return Helper.INSTANCE;
    }

    private static class MainHandler extends Handler {
        private MqMessageMonitor monitor;

        public MainHandler(MqMessageMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final Content content = (Content) msg.obj;
            switch (what) {
                case MSG_CODE_MESSAGE_ARRIVED:
                    monitor.onMessageArrived(content.topic, content.message);
                    break;
            }
        }
    }

    private static class Content {
        private String topic;
        private String message;

        private Content(String topic, String message){
            this.topic = topic;
            this.message = message;
        }
    }
}

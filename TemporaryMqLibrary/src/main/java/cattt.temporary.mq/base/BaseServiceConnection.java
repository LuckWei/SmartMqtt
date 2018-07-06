package cattt.temporary.mq.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import cattt.temporary.mq.base.model.IConnectionable;
import cattt.temporary.mq.logger.Log;
import cattt.temporary.mq.utils.IntentUtils;

import java.util.Timer;

public abstract class BaseServiceConnection implements ServiceConnection, IConnectionable {
    private static final int MSG_CODE_RECONNECT = 10000;
    private static Log logger = Log.getLogger(BaseServiceConnection.class);

    private Context context;
    private IBinder mBinder;
    private Timer mTimer;

    private MainHandler handler;

    public BaseServiceConnection() {
        handler = new MainHandler(this);
    }

    abstract protected String getConnectionType();

    abstract protected String getImplicitAction();

    abstract protected String getOwnCategory();

    @Override
    public void bindServiceOfMessage() {
        handler.sendEmptyMessage(MSG_CODE_RECONNECT);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        logger.w("onServiceConnected, componentName[%s/%s]" , name.getPackageName(), name.getClassName());
        mBinder = service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        logger.w("onServiceDisconnected, componentName[%s/%s]", name.getPackageName(), name.getClassName());
        mBinder = null;
    }

    @Override
    public IBinder getBinder() {
        return mBinder;
    }

    @Override
    public IConnectionable init(Context context) {
        if (context == null) {
            throw new NullPointerException(getConnectionType() + " -> context cannot null.");
        }
        this.context = context;
        return this;
    }

    @Override
    public void unbindService() throws IllegalArgumentException {
        if (mBinder != null) {
            context.unbindService(this);
            mBinder = null;
        }
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent();
        intent.setAction(getImplicitAction());
        intent.addCategory(getOwnCategory());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    @Override
    public void bindService() throws Exception {
        if (mBinder == null) {
            logger.i("attempted to bind [%s] service.", getConnectionType());
            if (TextUtils.isEmpty(getImplicitAction())) {
                throw new IllegalArgumentException(getConnectionType() + " -> method[getImplicitAction()] cannot null.");
            }
            Intent intent = IntentUtils.createExplicitFromImplicitIntent(context, getIntent());
            if (intent == null) {
                throw new NullPointerException("Intent cannot null.");
            }
            context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    private static class MainHandler extends Handler {

        private BaseServiceConnection connection;

        private MainHandler(BaseServiceConnection connection) {
            this.connection = connection;
        }

        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case MSG_CODE_RECONNECT:
                    try {
                        connection.bindService();
                    } catch (Exception ex) {
                        logger.e(String.format("Unable to bind [%s] service.", connection.getConnectionType()), ex);
                    }
                    break;
            }
        }
    }
}

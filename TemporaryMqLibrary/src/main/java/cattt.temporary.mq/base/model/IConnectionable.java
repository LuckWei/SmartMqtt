package cattt.temporary.mq.base.model;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public interface IConnectionable {
    void bindServiceOfMessage();

    IBinder getBinder();

    IConnectionable init(Context context);

    void bindService() throws Exception;

    void unbindService();

    Intent getIntent();
}

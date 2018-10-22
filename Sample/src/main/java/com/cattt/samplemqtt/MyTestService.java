package com.cattt.samplemqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class MyTestService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyTestBind();
    }

    private static class MyTestBind extends Binder {

    }
}

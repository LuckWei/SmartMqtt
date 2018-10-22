package com.cattt.samplemqtt;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import catt.kt.libs.mq.MqConfigure;
import catt.kt.libs.mq.MqControl;
import catt.kt.libs.mq.listeners.OnServiceConnectionListener;
import catt.kt.libs.mq.listeners.OnSubscribeMessagesListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnSubscribeMessagesListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.startBtn).setOnClickListener(this);
        findViewById(R.id.stopBtn).setOnClickListener(this);
        findViewById(R.id.sendMessageBtn).setOnClickListener(this);
        findViewById(R.id.subscribeBtn).setOnClickListener(this);
        findViewById(R.id.isConnectedBtn).setOnClickListener(this);

        MqConfigure.setServerUrl("You Mqtt service url, example: http://200.198.13.7:1883");
        MqConfigure.setUserName("You User Name");
        MqConfigure.setPassword("You Password");
        MqConfigure.setClientId("client id");
        MqConfigure.setTopics(new String[]{"1A2B3C4D5E6F7G8H"});


        MqControl.addOnSubscribeMessagesListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqControl.removeOnSubscribeMessagesListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (R.id.startBtn == id) {
            //重新对订阅内容进行赋值
            MqControl.bindService(getApplicationContext(), new OnServiceConnectionListener() {
                @Override
                public void onServiceConnected(ComponentName name) {
                    Log.e(TAG, "onServiceConnected: " + name.getPackageName() + "/" + name.getClassName());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.e(TAG, "onServiceDisconnected: " + name.getPackageName() + "/" + name.getClassName());
                }
            });
        }
        if (R.id.stopBtn == id) {
            try {
                MqControl.unbindService();
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "", ex);
            }
        }

        if (R.id.subscribeBtn == id) {
            MqControl.subscribe(MqConfigure.getTopics());
        }

        if (R.id.sendMessageBtn == id) {
            MqControl.publishMessage(MqConfigure.getTopics()[0], "{\"body\":{\"content\":\"亖\"},\"header\":{\"actionId\":12,\"sourceSender\":\"creater\",\"targetReceiver\":\"joiner\"}}");
        }

        if (R.id.isConnectedBtn == id) {
            Log.e(TAG, "MqControl.isConnected() = " + MqControl.isConnected());

        }
    }

    @Override
    public void onSubscribeMessage(String topic, int id, byte[] payload, int qos, boolean repeated, boolean retained) {
        Log.e(TAG, String.format("topic = %s, message = %s", topic, new String(payload)));
    }
}

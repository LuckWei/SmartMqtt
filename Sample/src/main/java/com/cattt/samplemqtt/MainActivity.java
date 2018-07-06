package com.cattt.samplemqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cattt.temporary.mq.MQ;
import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.MqMessageMonitor;
import cattt.temporary.mq.callback.OnConnectionListener;
import cattt.temporary.mq.callback.OnMqMessageListener;
import cattt.temporary.mq.logger.Log;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMqMessageListener {
    private static Log logger = Log.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.connectBtn).setOnClickListener(this);
        findViewById(R.id.disconnectBtn).setOnClickListener(this);
        MqMessageMonitor.get().addOnMqMessageListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqMessageMonitor.get().removeOnMqMessageListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (R.id.connectBtn == id) {
            //重新对订阅内容进行赋值
            MqConfigure.clientId = "cattt" + UUID.randomUUID().toString();
            MqConfigure.topics = new String[]{"catt/devices"};
            MQ.bindService(getApplicationContext(), new OnConnectionListener() {
                @Override
                public void onConnected() {
                    logger.e("onConnected()");
                }

                @Override
                public void onDisconnected() {
                    logger.e("onDisconnected()");
                }
            });
        }
        if (R.id.disconnectBtn == id) {
            try {
                MQ.unbindService();
            } catch (IllegalArgumentException ex) {
                logger.e("", ex);
            }
        }
    }

    @Override
    public void onMessageArrived(String topic, String message) {

    }
}

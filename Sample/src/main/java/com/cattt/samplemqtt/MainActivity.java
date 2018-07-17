package com.cattt.samplemqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cattt.temporary.mq.MQ;
import cattt.temporary.mq.MqConfigure;
import cattt.temporary.mq.wrapper.MqMessageMonitor;
import cattt.temporary.mq.callback.OnConnectionListener;
import cattt.temporary.mq.callback.OnMqMessageListener;
import cattt.temporary.mq.logger.Log;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMqMessageListener {
    private static Log logger = Log.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.startBtn).setOnClickListener(this);
        findViewById(R.id.stopBtn).setOnClickListener(this);
        findViewById(R.id.sendMessageBtn).setOnClickListener(this);
        findViewById(R.id.subscribeBtn).setOnClickListener(this);
        findViewById(R.id.isConnectedBtn).setOnClickListener(this);
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
        if (R.id.startBtn == id) {
            //重新对订阅内容进行赋值
            MqConfigure.clientId = "zhiwei_110101";
            MqConfigure.topics = new String[]{"1A2B3C4D5E6F7G8H"};
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
        if (R.id.stopBtn == id) {
            try {
                MQ.unbindService();
            } catch (IllegalArgumentException ex) {
                logger.e("", ex);
            }
        }

        if (R.id.subscribeBtn == id) {
            MQ.subscribe(MqConfigure.topics);
        }

        if (R.id.sendMessageBtn == id) {
            MQ.publishMessage(MqConfigure.topics[0], "哈哈哈哈哈哈你是小邋遢，邋遢大王就是你");
        }

        if (R.id.isConnectedBtn == id) {
            logger.e("isConnected = %b", MQ.isConnected());

        }
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        logger.e("topic = %s, message = %s", topic, message);
    }
}

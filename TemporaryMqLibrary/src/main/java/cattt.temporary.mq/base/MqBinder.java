package cattt.temporary.mq.base;

import android.os.Binder;

public class MqBinder extends Binder {
    private MqService mService;
    public MqBinder(MqService service){
        this.mService = service;
    }

    public void startConnect(){
        mService.startConnect();
    }

    public void publishMessage(String topic, String message){
        mService.publishMessage(topic, message);
    }

    public boolean isConnected(){
        return mService.isConnected();
    }
}

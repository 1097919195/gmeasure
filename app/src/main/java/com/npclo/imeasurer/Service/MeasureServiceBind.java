package com.npclo.imeasurer.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.npclo.imeasurer.base.BaseApplication;
import com.npclo.imeasurer.data.measure.MessageEvent;
import com.npclo.imeasurer.measure.MeasureFragment;
import com.npclo.imeasurer.measure.MeasurePresenter;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2018/3/21 0021.
 */

public class MeasureServiceBind extends Service {
    public static final String ACTION_START="ACTION_START";
    private final IBinder mbinder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("ahaha", "ahahah==service onBind");
        return mbinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("ahaha", "ahahah==service onCreate");
    }

    public class MyBinder extends Binder implements MeasureServiceInterface {

        public MeasureServiceBind getService() {
            return MeasureServiceBind.this;
        }

        @Override
        public void connectBle() {
            Log.e("ahaha", "ahahah==connectBle");

        }

        @Override
        public void setViewText() {
            Log.e("ahaha", "ahahah==setViewText");
        }
    }

    public void excute() {
        //通过Binder得到service的引用来调用servie内部的方法
        Log.e("ahaha", "ahahah==service excute");
        new MyServerThread().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("ahaha", "ahahah==service onStartCommand");
        if(ACTION_START.equals(intent.getAction())){
            //
        }
        return super.onStartCommand(intent, flags, startId);

    }

    class MyServerThread extends Thread{
        @Override
        public void run() {
            EventBus.getDefault().post(new MessageEvent(1));
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("ahaha", "ahahah==service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("ahaha", "ahahah==service onDestroy");
    }
}

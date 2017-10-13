package com.npclo.gdemo.base;

import android.app.Application;
import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.squareup.leakcanary.LeakCanary;

import java.util.UUID;

import rx.Observable;

/**
 * Created by Endless on 2017/7/19.
 */

public class BaseApplication extends Application {
    private RxBleClient rxBleClient;
    private RxBleDevice rxBleDevice;
    private UUID characteristicUUID;
    private boolean haveUpdate = false;
    private boolean isFirstCheckUpdate = true;
    private Observable<RxBleConnection> connectionObservable;

    public static RxBleClient getRxBleClient(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.rxBleClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: 2017/8/2 判断当前手机API版本
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            // TODO: 2017/8/28 heap analysis
            return;
        }
        LeakCanary.install(this);
        rxBleClient = RxBleClient.create(this);
    }

    public static RxBleDevice getRxBleDevice(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.rxBleDevice;
    }

    public static void setRxBleDevice(Context context, RxBleDevice rxBleDevice) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.rxBleDevice = rxBleDevice;
    }

    public static void setNotificationInfo(Context context, UUID characteristicUUID, Observable<RxBleConnection> connectionObservable) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.characteristicUUID = characteristicUUID;
        application.connectionObservable = connectionObservable;
    }

    public static UUID getUUID(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.characteristicUUID;
    }

    public static Observable<RxBleConnection> getConnection(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.connectionObservable;
    }

    /*=================TODO  elegant handle this=====================**/
    public static void haveUpdate(Context context, boolean b) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.haveUpdate = b;
    }

    public static boolean canUpdate(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.haveUpdate;
    }

    public static void setIsFirstCheck(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.isFirstCheckUpdate = false;
    }

    public static boolean getFirstCheckHint(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.isFirstCheckUpdate;
    }
}

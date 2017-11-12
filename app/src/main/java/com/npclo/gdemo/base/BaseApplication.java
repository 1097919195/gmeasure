package com.npclo.gdemo.base;

import android.app.Application;
import android.content.Context;

import com.polidea.rxandroidble.RxBleClient;
import com.squareup.leakcanary.LeakCanary;

import java.util.UUID;

/**
 * @author Endless
 * @date 2017/7/19
 */

public class BaseApplication extends Application {
    private RxBleClient rxBleClient;
    private UUID characteristicUUID;
    private String macAddress;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        rxBleClient = RxBleClient.create(this);
    }

    public static RxBleClient getRxBleClient(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.rxBleClient;
    }

    public static void setNotificationUUID(Context context, UUID characteristicUUID) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.characteristicUUID = characteristicUUID;
    }

    public static UUID getUUID(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.characteristicUUID;
    }

    public static void setBleAddress(Context context, String macAddress) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.macAddress = macAddress;
    }

    public static String getMacAddress(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.macAddress;
    }
}

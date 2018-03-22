package com.npclo.imeasurer.base;

import android.app.Application;
import android.content.Context;

import com.npclo.imeasurer.data.measure.Item;
import com.npclo.imeasurer.utils.CrashHandler;
import com.polidea.rxandroidble.RxBleClient;
import com.squareup.leakcanary.LeakCanary;

import java.util.List;

/**
 * @author Endless
 * @date 2017/7/19
 */

public class BaseApplication extends Application {
    private RxBleClient rxBleClient;
    public static Context getAppContext;
    private List<Item> angleList;

    public static RxBleClient getRxBleClient(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.rxBleClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getAppContext = getApplicationContext();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        rxBleClient = RxBleClient.create(this);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    public static List<Item> getAngleList(Context context) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        return application.angleList;
    }

    public static void setAngleList(Context context, List<Item> list) {
        BaseApplication application = ((BaseApplication) context.getApplicationContext());
        application.angleList = list;
    }
}

package com.npclo.imeasurer.measure;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.errorprone.annotations.Var;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.Service.MeasureServiceBind;
import com.npclo.imeasurer.Service.MeasureServiceInterface;
import com.npclo.imeasurer.base.BaseActivity;
import com.npclo.imeasurer.base.BaseApplication;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.UUID;

/**
 * @author Endless
 * @date 10/12/2017
 */

public class MeasureActivity extends BaseActivity {
    private MeasureServiceBind measureServiceBind;
    private MeasureServiceInterface measureServiceInterface = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        //绑定服务
        Intent measureService = new Intent(MeasureActivity.this,MeasureServiceBind.class);
        bindService(measureService, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
            MeasureFragment measureFragment = findFragment(MeasureFragment.class);
        if (measureFragment == null) {
            measureFragment = MeasureFragment.newInstance();
            loadRootFragment(R.id.content_frame, measureFragment);
            PreferencesUtils instance = PreferencesUtils.getInstance(this);
            float measureOffset = instance.getMeasureOffset();
            String macAddress = instance.getMacAddress();
            String deviceUuid = instance.getDeviceUuid();
            if (!TextUtils.isEmpty(macAddress) && !TextUtils.isEmpty(deviceUuid)) {
                RxBleDevice device = BaseApplication.getRxBleClient(this).getBleDevice(macAddress);
                new MeasurePresenter(measureFragment, SchedulerProvider.getInstance(), measureOffset,
                        macAddress, device, UUID.fromString(deviceUuid));
            } else {
                Toast.makeText(this, "未连接蓝牙设备", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //activity与service绑定时
    private ServiceConnection connection = new ServiceConnection() {
        //系统调用这个来传送在service的onBind()中返回的IBinder．
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //绑定后就可以使用Service的相关方法和属性来开始你对Service的操作
            measureServiceInterface = (MeasureServiceInterface) iBinder;
            if (measureServiceInterface != null) {
                measureServiceInterface.connectBle();
                measureServiceInterface.setViewText();
            }

            measureServiceBind = ((MeasureServiceBind.MyBinder) iBinder).getService();
            measureServiceBind.excute();//执行service的方法
//            Intent intent = new Intent();
//            measureServiceBind.onStartCommand(intent, 0, 0);
        }

        //Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            measureServiceBind = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}

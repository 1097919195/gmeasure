package com.npclo.imeasurer.measure;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.npclo.imeasurer.R;
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

    private MeasureFragment measureFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        measureFragment = findFragment(MeasureFragment.class);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            measureFragment.showPopWindow();
        }
    }
}

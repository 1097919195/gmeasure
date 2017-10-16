package com.npclo.gdemo.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseActivity;
import com.npclo.gdemo.main.home.HomeContract;
import com.npclo.gdemo.main.home.HomeFragment;
import com.npclo.gdemo.main.home.HomePresenter;
import com.npclo.gdemo.utils.schedulers.SchedulerProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

import java.util.UUID;

import rx.Observable;

/**
 * Created by Endless on 2017/9/1.
 */

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private RxPermissions rxPermissions;
    private RxBleDevice rxBleDevice;
    private UUID characteristicUUID;
    private Observable<RxBleConnection> connectionObservable;
    private RxBleClient rxBleClient;
    public SpeechSynthesizer speechSynthesizer;
    private HomeContract.Presenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        init();
    }

    private void init() {
//        PermissionGen.with(MainActivity.this)
//                .addRequestCode(100)
//                .permissions(
//                        Manifest.permission_group.LOCATION,
//                        Manifest.permission_group.STORAGE,
//                        Manifest.permission_group.CAMERA)
//                .request();
        rxPermissions = new RxPermissions(this);
        rxBleClient = RxBleClient.create(this);
        initSpeech();
        //加载登录后的欢迎界面
        HomeFragment homeFragment = findFragment(HomeFragment.class);
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.content_frame, homeFragment);
            mPresenter = new HomePresenter(rxBleClient, homeFragment, SchedulerProvider.getInstance());
            homeFragment.setPresenter(mPresenter);
        }
    }

    public RxBleClient getRxBleClient() {
        return rxBleClient;
    }

    public RxPermissions getRxPermissions() {
        return rxPermissions;
    }

    protected void initView() {
        setContentView(R.layout.act_main);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = null;
        try {
            Bundle bundle = data.getExtras();
            result = bundle.getString("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (resultCode) {
            case SCAN_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithId(result);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.scan_qrcode_failed), Toast.LENGTH_LONG).show();
                }
                break;
            case CODE_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithCode(result);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.enter_qrcode_error), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public RxBleDevice getRxBleDevice() {
        return rxBleDevice;
    }

    public void setRxBleDevice(RxBleDevice rxBleDevice) {
        this.rxBleDevice = rxBleDevice;
    }

    public UUID getCharacteristicUUID() {
        return characteristicUUID;
    }

    public void setCharacteristicUUID(UUID characteristicUUID) {
        this.characteristicUUID = characteristicUUID;
    }

    public Observable<RxBleConnection> getConnectionObservable() {
        return connectionObservable;
    }

    public void setConnectionObservable(Observable<RxBleConnection> connectionObservable) {
        this.connectionObservable = connectionObservable;
    }

    private void initSpeech() {
        String APPKEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
        String APPSECRET = "29aa998c451d64d9334269546a4021b8";
        if (speechSynthesizer == null)
            speechSynthesizer = new SpeechSynthesizer(this, APPKEY, APPSECRET);
        speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
        speechSynthesizer.setTTSListener(new SpeechSynthesizerListener() {
            @Override
            public void onEvent(int i) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
        speechSynthesizer.init(null);// FIXME: 2017/8/24 语音播报需要联网
    }
}
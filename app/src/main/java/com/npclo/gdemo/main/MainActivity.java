package com.npclo.gdemo.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseActivity;
import com.npclo.gdemo.base.BaseApplication;
import com.npclo.gdemo.main.home.HomeContract;
import com.npclo.gdemo.main.home.HomeFragment;
import com.npclo.gdemo.main.home.HomePresenter;
import com.npclo.gdemo.utils.schedulers.SchedulerProvider;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

/**
 * @author Endless
 */
public class MainActivity extends BaseActivity {
    private RxPermissions rxPermissions;
    public SpeechSynthesizer speechSynthesizer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSpeech();
    }

    private void init() {
        rxPermissions = new RxPermissions(this);
        //加载登录后的欢迎界面
        HomeFragment homeFragment = findFragment(HomeFragment.class);
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.content_frame, homeFragment);
            HomeContract.Presenter mPresenter = new HomePresenter(BaseApplication.getRxBleClient(this),
                    homeFragment, SchedulerProvider.getInstance());
            homeFragment.setPresenter(mPresenter);
        }
    }

    public RxPermissions getRxPermissions() {
        return rxPermissions;
    }

    protected void initView() {
        setContentView(R.layout.act_main);
    }

    private void initSpeech() {
        String APPKEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
        String APPSECRET = "29aa998c451d64d9334269546a4021b8";
        if (speechSynthesizer == null) {
            speechSynthesizer = new SpeechSynthesizer(this, APPKEY, APPSECRET);
        }
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

    @Override
    protected void onStop() {
        super.onStop();
        speechSynthesizer = null;
    }
}
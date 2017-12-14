package com.npclo.imeasurer.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.npclo.imeasurer.R;
import com.npclo.imeasurer.data.app.App;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.ApiException;
import com.npclo.imeasurer.utils.Gog;
import com.polidea.rxandroidble.exceptions.BleException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import me.yokeyword.fragmentation.SupportFragment;
import retrofit2.HttpException;
import util.UpdateAppUtils;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public abstract class BaseFragment extends SupportFragment {
    protected View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
        initView(mRootView);
        initComToolbar();
        return mRootView;
    }

    /**
     * app默认页面的沉寂式toolbar的初始化
     * 目前暂无更多的导航需要
     */
    protected void initComToolbar() {
    }

    /**
     * 其他页面的toolbar一些共同设置
     *
     * @param toolbar
     */
    protected void navOfToolbar(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.mipmap.left);
        toolbar.setNavigationOnClickListener(view ->
                startActivity(new Intent(getActivity(), MainActivity.class))
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View mRootView);

    protected void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String text, int length) {
        Toast.makeText(getActivity(), text, length).show();
    }

    protected void showSnackbar(String text) {
        Snackbar.make(checkNotNull(getView()), text, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 统一处理错误 RxJava调用
     *
     * @param e 异常
     */
    public void onHandleError(Throwable e) {
        if (e instanceof SocketTimeoutException) {
            showToast(getString(R.string.net_connect_timeout));
        } else if (e instanceof ConnectException) {
            showToast(getString(R.string.net_connect_out));
        } else if (e instanceof HttpException) {
            showToast(getString(R.string.service_down));
        } else if (e instanceof ApiException) {
            showToast(e.getMessage());
        } else if (e instanceof BleException) {
            showToast("蓝牙设备异常，请重试");
            toast2Speech("蓝牙设备异常，请重试");
            Gog.e("蓝牙异常===" + e.toString());
        } else {
            String message = getStackMsg(e);
            Gog.e(message);
        }
    }

    protected void toast2Speech(String s) {
    }

    protected int getVersionCode() {
        PackageManager manager = getActivity().getPackageManager();
        try {
            return manager.getPackageInfo(getActivity().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    protected void updateApp(App app) {
        BaseApplication.haveUpdate(getActivity(), true);
        UpdateAppUtils.from(getActivity())
                .serverVersionCode(app.getCode())
                .serverVersionName(app.getVersion())
                .apkPath(app.getPath() + "?v=" + app.getVersion())
                .updateInfo(app.getInfo().trim())
                .update();
    }

    private String getStackMsg(Exception e) {

        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (int i = 0; i < stackArray.length; i++) {
            StackTraceElement element = stackArray[i];
            sb.append(element.toString() + "\n");
        }
        return sb.toString();
    }

    private String getStackMsg(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (StackTraceElement el : stackArray) {
            sb.append(el.toString() + "\n");
        }
        return sb.toString();
    }

}
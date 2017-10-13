package com.npclo.gdemo.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.npclo.gdemo.R;
import com.npclo.gdemo.utils.ApiException;
import com.polidea.rxandroidble.exceptions.BleException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import me.yokeyword.fragmentation.SupportFragment;
import retrofit2.HttpException;
import util.UpdateAppUtils;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public abstract class BaseFragment extends SupportFragment {
    protected View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mRootView) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
        initView(mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        afterCreate(savedInstanceState);
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View mRootView);

    protected void afterCreate(Bundle savedInstanceState) {
    }

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
     * @param e
     * @param TAG
     */
    protected void handleError(Throwable e, String TAG) {
        if (e instanceof SocketTimeoutException) {
            showToast(getString(R.string.net_connect_timeout));
        } else if (e instanceof ConnectException) {
            showToast(getString(R.string.net_connect_out));
        } else if (e instanceof HttpException) {
            showToast(getString(R.string.service_down));
        } else if (e instanceof ApiException) {
            showToast(e.getMessage());
        } else if (e instanceof BleException) {
            showToast("蓝牙设备异常，请重试", Toast.LENGTH_LONG);
            toast2Speech("蓝牙设备异常，请重试");
        } else {
            showToast(getString(R.string.something_error));
        }
    }

    protected void toast2Speech(String s) {
    }

    protected int getVersionCode() {
        PackageManager manager = getActivity().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;

    }

}
package com.npclo.imeasurer.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.AccountActivity;
import com.npclo.imeasurer.data.App;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.Gog;
import com.npclo.imeasurer.utils.LogUtils;
import com.npclo.imeasurer.utils.exception.ApiException;
import com.npclo.imeasurer.utils.exception.TimeoutException;
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
    private Handler handler = new android.os.Handler();

//    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (savedInstanceState != null) {
//            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
//
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            if (isSupportHidden) {
//                ft.hide(this);
//            } else {
//                ft.show(this);
//            }
//            ft.commit();
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
//    }


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
        toolbar.setNavigationOnClickListener(view -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });
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
        Snackbar snackbar = Snackbar.make(checkNotNull(getView()), text, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        View view = snackbar.getView();
        view.setBackgroundColor(getResources().getColor(R.color.primary));
        snackbar.show();
    }

    /**
     * 统一处理错误 RxJava调用
     *
     * @param e 异常
     */
    protected void onHandleError(Throwable e) {
        if (e instanceof SocketTimeoutException) {
            showToast(getString(R.string.net_connect_timeout));
        } else if (e instanceof ConnectException) {
            showToast(getString(R.string.net_connect_out));
        } else if (e instanceof HttpException) {
            showToast(getString(R.string.service_down));
        } else if (e instanceof ApiException) {
            showToast(e.getMessage());
        } else if (e instanceof TimeoutException) {
            showToast(e.getMessage());
            handler.postDelayed(this::goToSignIn, 1000);
        } else if (e instanceof BleException) {
            showToast(getResources().getString(R.string.ble_error_hint));
            toast2Speech(getResources().getString(R.string.ble_error_hint));
        } else if (e instanceof JsonSyntaxException) {
            Gog.e(e.getMessage() + e.getCause().toString());
        } else {
            String message = LogUtils.getStackMsg(e);
            LogUtils.fixBug("异常：" + e.getClass().getSimpleName() + "\n异常信息：" + e.getMessage() + "\n详细原因: " + message);

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
        UpdateAppUtils.from(getActivity())
                .serverVersionCode(app.getCode())
                .serverVersionName(app.getVersion())
                .apkPath(app.getPath() + "?v=" + app.getVersion())
                .updateInfo(app.getInfo().trim())
                .update();
    }

    private void goToSignIn() {
        FragmentActivity activity = getActivity();
        startActivity(new Intent(activity, AccountActivity.class));
        activity.finish();
    }
}
package com.npclo.imeasurer.user.manage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.AccountActivity;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 * 更改密码界面
 */
public class ManageFragment extends BaseFragment implements ManageContract.View {
    @BindView(R.id.support_frag_toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_old_pwd)
    AppCompatEditText inputOldPwd;
    @BindView(R.id.input_new_pwd1)
    AppCompatEditText inputNewPwd1;
    @BindView(R.id.input_new_pwd2)
    AppCompatEditText inputNewPwd2;
    @BindView(R.id.action_submit)
    AppCompatButton actionResetPwd;
    Unbinder unbinder;
    @NonNull
    private ManageContract.Presenter presenter;
    private MaterialDialog dialog;
    private String newpwd1;

    @Override
    public void setPresenter(@NonNull ManageContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_manage;
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        toolbar.setTitle("修改密码");
        navOfToolbar(toolbar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.action_submit)
    public void onViewClicked() {
        String old = inputOldPwd.getText().toString();
        if (checkInput(old)) {
            showSnackbar("旧密码格式不正确");
            return;
        }
        newpwd1 = inputNewPwd1.getText().toString();
        if (checkInput(newpwd1)) {
            showSnackbar("新密码格式不正确");
            return;
        }
        String newpwd2 = inputNewPwd2.getText().toString();
        if (checkInput(newpwd2)) {
            showSnackbar("再次输入密码格式不正确");
            return;
        }
        if (!newpwd1.equals(newpwd2)) {
            showSnackbar("新输入密码两次不一致，请检查");
            return;
        }
        presenter.resetPwd(old, newpwd1);
    }

    private boolean checkInput(String field) {
        if (TextUtils.isEmpty(field)) {
            return true;
        }
        return field.length() < 6;
    }

    public static ManageFragment newInstance() {
        return new ManageFragment();
    }

    @Override
    public void showLoading(boolean bool) {
        if (bool) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .progress(true, 100)
                    .backgroundColor(getResources().getColor(R.color.white))
                    .show();
        } else {
            dialog.dismiss();
        }
    }

    @Override
    public void showEditSuccess() {
        showLoading(false);
        showSnackbar("修改成功，请重新登录");
        FragmentActivity activity = getActivity();
        PreferencesUtils.getInstance(activity).setToken("");
        PreferencesUtils.getInstance(activity).setLoginPwd(newpwd1);
        (new android.os.Handler()).postDelayed(() -> {
            Intent intent = new Intent(activity, AccountActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }, 1000);
    }

    @Override
    public void showEditError(Throwable e) {
        showLoading(false);
        onHandleError(e);
    }

    @Override
    public void showEditCompleted() {
        showLoading(false);
    }
}
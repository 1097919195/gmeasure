package com.npclo.imeasurer.account.signin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.data.User;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SignInFragment extends BaseFragment implements SignInContract.View {
    Unbinder unbinder;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.input_name)
    EditText inputName;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.input_eye)
    ImageView inputEye;
    @BindView(R.id.action_remember_pwd)
    CheckBox actionRememberPwd;
    //    @BindView(R.id.action_sign_in)
//    AppCompatButton actionSignIn;
//    @BindView(R.id.forget_pwd_tv)
//    TextView forgetPwdTv;
//    @BindView(R.id.signup_tv)
//    TextView signupTv;
    @BindView(R.id.action_wechat_login)
    ImageView actionWechatLogin;
    @BindView(R.id.sign_in_frag)
    ScrollView signInFrag;

    private SignInContract.Presenter signinPresenter;
    private boolean pwdLabel = true;
    @NonNull
    private Boolean isUserRememberPwd = true;
    private String name;
    private String password;
    private MaterialDialog signInLoadingDialog;

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (signinPresenter != null) {
            signinPresenter.subscribe();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (signinPresenter != null) {
            signinPresenter.unsubscribe();
        }
    }

    @Override
    public void setPresenter(SignInContract.Presenter presenter) {
        signinPresenter = checkNotNull(presenter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_signin;
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
        String loginName = instance.getLoginName();
        String loginPwd = instance.getLoginPwd();
        inputName.setText(loginName);
        inputPassword.setText(loginPwd);
    }

    private boolean validate() {
        boolean valid = true;
        name = inputName.getText().toString();
        password = inputPassword.getText().toString();
        if (name.isEmpty()) {
            inputName.setError(getActivity().getString(R.string.name_enter_valid));
            valid = false;
        } else {
            inputName.setError(null);
        }
        if (password.isEmpty() || password.length() < 6 || password.length() > 20) {
            inputPassword.setError(getActivity().getString(R.string.pwd_enter_valid));
            valid = false;
        } else {
            inputPassword.setError(null);
        }
        return valid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.logo, R.id.input_eye, R.id.action_remember_pwd, R.id.action_sign_in,
            R.id.action_wechat_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.logo:
                break;
            case R.id.input_eye:
                if (pwdLabel) {
                    inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pwdLabel = false;
                } else {
                    inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwdLabel = true;
                }
                break;
            case R.id.action_remember_pwd:
                RxCompoundButton.checkedChanges(actionRememberPwd).subscribe(bool -> isUserRememberPwd = bool);
                break;
            case R.id.action_sign_in:
                if (!validate()) {
                    return;
                }
                signinPresenter.signIn(name, password);
                break;
//            case R.id.forget_pwd_tv:
//                ForgetPwdFragment fragment = ForgetPwdFragment.newInstance();
//                start(fragment, SINGLETASK);
//                fragment.setPresenter(new ForgetPwdPresenter(fragment,
//                        SchedulerProvider.getInstance()));
//                break;
//            case R.id.signup_tv:
//                SignUpFragment signUpFragment = SignUpFragment.newInstance();
//                start(signUpFragment, SINGLETASK);
//                signUpFragment.setPresenter(new SignUpPresenter(signUpFragment,
//                        SchedulerProvider.getInstance()));
//                break;
            case R.id.action_wechat_login:
                showToast("待开放微信登录");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        hideSoftInput();
    }

    @Override
    public void showSignInSuccess(User user) {
        showToast(getResources().getString(R.string.login_success_hint));
        PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
        if (isUserRememberPwd) {
            instance.setLoginPwd(password);
        } else {
            instance.setLoginPwd("");
        }
        instance.setLoginName(name);

        instance.setUserId(user.getId());
        instance.setUserName(user.getName());
        instance.setUserNickname(user.getNickname());
        instance.setUserOrgid(user.getOrgId());
        instance.setCurrTimes(user.getCurrTimes());
        instance.setTotalTimes(user.getTotalTimes());

        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void showSignInError(Throwable e) {
        signInLoadingDialog.dismiss();
        onHandleError(e);
    }

    @Override
    public void completeSignIn() {
        showLoading(false);
    }

    @Override
    public void showLoading(boolean bool) {
        if (bool) {
            signInLoadingDialog = new MaterialDialog.Builder(getActivity())
                    .progress(true, 100)
                    .backgroundColor(getResources().getColor(R.color.white))
                    .show();
        } else {
            signInLoadingDialog.dismiss();
        }
    }

    @Override
    public void saveToken(String token) {
        PreferencesUtils.getInstance(getActivity()).setToken(token);
    }
}
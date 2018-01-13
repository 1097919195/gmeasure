package com.npclo.imeasurer.account.signup;

import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.signin.SignInFragment;
import com.npclo.imeasurer.account.signin.SignInPresenter;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.data.User;
import com.npclo.imeasurer.data.ValidCode;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SignUpFragment extends BaseFragment implements SignUpContract.View {

    @BindView(R.id.input_mobile)
    EditText inputMobile;
    @BindView(R.id.input_valid_code)
    EditText inputValidCode;
    @BindView(R.id.action_valid_code)
    AppCompatButton actionValidCode;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.input_eye)
    ImageView inputEye;
    @BindView(R.id.action_sign_up)
    AppCompatButton actionSignUp;
    @BindView(R.id.to_sign_in)
    LinearLayout toSignIn;
    private SignUpContract.Presenter signUpPresenter;
    private Unbinder bind;
    private String name;
    private String pwd;
    private boolean pwd_label = true;
    private MaterialDialog dialog;
    private String code;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_signup;
    }

    @Override
    protected void initView(View mRootView) {
        bind = ButterKnife.bind(this, this.mRootView);
    }

    private boolean checkInputValid() {
        Boolean flag = true;
        name = inputMobile.getText().toString();
        pwd = inputPassword.getText().toString();
        code = inputValidCode.getText().toString();
        if (TextUtils.isEmpty(name)) {
            inputMobile.setError(getActivity().getString(R.string.name_enter_valid));
            flag = false;
        }

        if (TextUtils.isEmpty(pwd) || pwd.length() < 6 || pwd.length() > 20) {
            inputPassword.setError(getActivity().getString(R.string.pwd_enter_valid));
            flag = false;
        }
        if (TextUtils.isEmpty(code) || code.length() != 6) {
            inputValidCode.setError(getActivity().getString(R.string.plz_valid_code));
            flag = false;
        }
        return flag;
    }

    @Override
    public void showSignUpSuccess(User user) {
        showToast(getResources().getString(R.string.register_success_hint));
        PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
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
    public void showSignUpError(Throwable e) {
        onHandleError(e);
        showLoading(false);
    }

    @Override
    public void completeSignUp() {
        showLoading(false);
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
    public void showValidCodeSendSuccess(ValidCode code) {
        showToast(getString(R.string.valid_code_send_success) + code.getCode());
    }

    @Override
    public void showValidCodeSendError(Throwable e) {
        onHandleError(e);
        dialog.dismiss();
    }

    @Override
    public void showCompleteGetValidCode() {
        showLoading(false);
    }

    @Override
    public void setPresenter(SignUpContract.Presenter presenter) {
        signUpPresenter = checkNotNull(presenter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    @OnClick({R.id.action_valid_code, R.id.input_eye, R.id.action_sign_up, R.id.to_sign_in})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_valid_code:
                String mobile = inputMobile.getText().toString();
                if (mobile.length() == 0 || mobile.length() != 11) {
                    // TODO: 2017/8/30 check mobile number valid
                    showToast(getString(R.string.plz_valid_mobile));
                    return;
                }
                signUpPresenter.getValidCode(mobile, "signup");
                break;
            case R.id.input_eye:
                if (pwd_label) {
                    inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pwd_label = false;
                } else {
                    inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    pwd_label = true;
                }
                break;
            case R.id.action_sign_up:
                if (!checkInputValid()) {
                    return;
                }
                signUpPresenter.signUp(name, pwd, code);
                break;
            case R.id.to_sign_in:
                SignInFragment fragment = SignInFragment.newInstance();
                start(fragment, SINGLETASK);
                fragment.setPresenter(new SignInPresenter(fragment,
                        SchedulerProvider.getInstance()));
                break;
            default:
                break;
        }
    }
}

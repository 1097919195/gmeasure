package com.npclo.imeasurer.measure;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.npclo.imeasurer.BuildConfig;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseApplication;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.camera.CaptureActivity;
import com.npclo.imeasurer.data.IUser;
import com.npclo.imeasurer.data.ThirdMember;
import com.npclo.imeasurer.data.WechatUser;
import com.npclo.imeasurer.data.measure.Item;
import com.npclo.imeasurer.data.measure.Measurement;
import com.npclo.imeasurer.data.measure.MessageEvent;
import com.npclo.imeasurer.data.measure.Part;
import com.npclo.imeasurer.data.measure.Result;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.BitmapUtils;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.Gog;
import com.npclo.imeasurer.utils.LogUtils;
import com.npclo.imeasurer.utils.MeasureStateEnum;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.exception.ApiException;
import com.npclo.imeasurer.utils.views.MyGridView;
import com.npclo.imeasurer.utils.views.MyTextView;
import com.polidea.rxandroidble.exceptions.BleGattException;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static com.npclo.imeasurer.R.id.measure_stat_al;

/**
 * @author Endless
 */
public class MeasureFragment extends BaseFragment implements MeasureContract.View {
    public static final int BATTERY_LOW = 30;
    public static final int BATTERY_HIGH = 80;
    public static final String FEMALE = "女";
    public static final String MALE = "男";
    public static final String JPG_SUFFIX = ".jpg";
    @BindView(R.id.support_frag_toolbar)
    Toolbar toolbar;
    @BindView(R.id.wechat_icon)
    ImageView wechatIcon;
    @BindView(R.id.wechat_nickname)
    TextView wechatNickname;
    @BindView(R.id.wechat_gender)
    TextView wechatGender;
    @BindView(R.id.camera_add)
    RelativeLayout cameraAdd;
    @BindView(R.id.save_measure_result)
    AppCompatButton btnSave;
    @BindView(R.id.next_person)
    AppCompatButton btnNext;
    @BindView(R.id.measure_table_layout)
    MyGridView gridView;
    @BindView(R.id.wechat_gender_edit)
    LinearLayout genderLine;
    @BindView(R.id.img_1)
    ImageView img1;
    @BindView(R.id.img_2)
    ImageView img2;
    @BindView(R.id.img_3)
    ImageView img3;
    @BindView(R.id.del_1)
    ImageView del1;
    @BindView(R.id.del_2)
    ImageView del2;
    @BindView(R.id.del_3)
    ImageView del3;
    @BindView(R.id.frame_1)
    FrameLayout frame1;
    @BindView(R.id.frame_2)
    FrameLayout frame2;
    @BindView(R.id.frame_3)
    FrameLayout frame3;
    Unbinder unbinder;
    @BindView(R.id.user_layout)
    LinearLayout userLayout;
    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.measure_type)
    TextView measureType;
    @BindView(measure_stat_al)
    TextView measureStatAl;
    @BindView(R.id.measure_stat_no)
    TextView measureStatNo;
    @BindView(R.id.contract_stat)
    LinearLayout contractStat;
    @BindView(R.id.unmeasured_item_hint)
    AppCompatTextView unmeasuredItemHint;
    private MeasureContract.Presenter measurePresenter;
    private IUser measurer;
    private SpeechSynthesizer speechSynthesizer;
    private MaterialDialog saveProgressbar;
    public static final int TAKE_PHOTO = 13;
    public static final int CROP_PHOTO = 14;
    private static final int IMAGE_REQUEST_CODE = 15;
    public static final int DISPLAY_PHOTO = 16;
    private List<FrameLayout> unVisibleView = new ArrayList<>();
    private List<LinearLayout> unMeasuredList = new ArrayList<>();
    private List<String> angleList;
    private List<Part> partList = new ArrayList<>();
    private LinearLayout modifyingView;
    private boolean initUmMeasureListFlag;
    private Uri imageUri;
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private String[] measureSequence;
    public static final File PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    private String picName;
    private String firstMeasurePartName;
    public static final String REDIRECT_URI = "redirect_uri";

    public static MeasureFragment newInstance() {
        return new MeasureFragment();
    }

    @Override
    public void setPresenter(MeasureContract.Presenter presenter) {
        measurePresenter = checkNotNull(presenter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_measure;
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        //注册EventBus
        EventBus.getDefault().register(this);
        //初始化需要测量角度的部位
        angleList = initMeasureAnglePartsList();
        //渲染测量部位列表
        initMeasureItemList(angleList);
        // TODO: 2017/9/4 使用RecyclerView替代
        // FIXME: 2017/9/8 notifyItemChanged 部分绑定
        ItemAdapter adapter = new ItemAdapter(getActivity(), R.layout.list_measure_item, (ArrayList<Part>) partList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((AdapterView<?> var1, View view, int position, long var4) -> {
            resetTextViewClickState();
            MyTextView textView = (MyTextView) ((LinearLayout) view).getChildAt(0);
            String cn = textView.getText().toString();
            //只有处于已测量的部位才能修改，未测量部位不能修改
            if (textView.getState() == MeasureStateEnum.MEASURED.ordinal()) {
                textView.setTextColor(getResources().getColor(R.color.modifying));
                textView.setState(MeasureStateEnum.MODIFYING.ordinal());
                unmeasuredItemHint.setText(cn);
                speechSynthesizer.playText("重新测量部位" + cn);
                modifyingView = ((LinearLayout) view);
            }
        });
        unVisibleView.clear();
        unVisibleView.add(frame1);
        unVisibleView.add(frame2);
        unVisibleView.add(frame3);

        initToolbar();

        PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
        String contractName = instance.getContractName();
        int unMeasuredPersons = instance.getMeasureNum();
        int measured = instance.getMeasureMeasured();

        if (!TextUtils.isEmpty(contractName)) {
            contractStat.setVisibility(View.VISIBLE);
            measureType.setText(contractName);
            measureStatAl.setText(String.valueOf(measured));
            measureStatNo.setText(String.valueOf(unMeasuredPersons));
        }
        initSpeech();
    }

    @NonNull
    private List<String> initMeasureAnglePartsList() {
        String[] preArray = getResources().getStringArray(R.array.angle_items);
        List<Item> setArray = BaseApplication.getAngleList(getActivity());
        List<String> angleList = new ArrayList<>(Arrays.asList(preArray));
        int l = setArray.size();
        if (l > 0) {
            for (int i = 0; i < l; i++) {
                String cn = setArray.get(i).getName();
                if (!angleList.contains(cn)) {
                    angleList.add(cn);
                }
            }
        }
        return angleList;
    }

    private void initToolbar() {
        toolbar.setTitle("量体");
        toolbar.inflateMenu(R.menu.base_toolbar_menu);
        toolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_unknown);
        toolbar.setNavigationIcon(R.mipmap.left);
        toolbar.setNavigationOnClickListener(view -> showHandleBackPress());
    }

    private void resetTextViewClickState() {
        for (int i = 0, count = gridView.getChildCount(); i < count; i++) {
            MyTextView textView = (MyTextView) ((LinearLayout) gridView.getChildAt(i)).getChildAt(0);
            if (textView.getState() == MeasureStateEnum.MODIFYING.ordinal()) {
                textView.setState(MeasureStateEnum.MEASURED.ordinal());
                textView.setTextColor(getResources().getColor(R.color.measured));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //初始化语音播报
        if (measurePresenter != null) {
            measurePresenter.subscribe();
        }
        unmeasuredItemHint.setText(firstMeasurePartName);

        measurer = getActivity().getIntent().getBundleExtra("userBundle").getParcelable("user");
        //仅接收homefragment传值过来的用户信息时才赋值，从当前fragment发起的意图返回结果不在此处进行赋值调用
        if (measurer != null) {
            if (measurer instanceof WechatUser) {
                setWechatUserInfo((WechatUser) measurer);
            } else if (measurer instanceof ThirdMember) {
                setThirdMemberInfo((ThirdMember) measurer);
            }
        }

        initUmMeasureListFlag = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String s2;
        if (unMeasuredList.size() > 0) {
            s2 = "当前测量部位";
            firstMeasurePartName = ((MyTextView) unMeasuredList.get(0).getChildAt(0)).getText().toString();
        } else {
            s2 = "请确定待测人员性别，首先测量部位";
            if (measureSequence == null) {
                firstMeasurePartName = partList.get(0).getCn();
            } else {
                firstMeasurePartName = measureSequence[0];
            }
        }
        speechSynthesizer.playText(s2 + firstMeasurePartName);
    }

    private void setWechatUserInfo(WechatUser u) {
        wechatNickname.setText(u.getNickname());
        if (u.getGender() != 0) {
            wechatGender.setText(u.getGender() == 1 ? "男" : "女");
        } else {
            switchGender();
        }
        if (u.getAvatar() != null) {
            Glide.with(getActivity()).load(u.getAvatar())
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.drawable.load_fail_pic))
                    .into(wechatIcon);
        } else {
            wechatIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        if (u.getTimes() > 0) {
            showToast("该用户已量体" + u.getTimes() + "次");
        }
    }

    private void setThirdMemberInfo(ThirdMember member) {
        wechatNickname.setText(member.getName());
        if (member.getGender() != 0) {
            wechatGender.setText(member.getGender() == 1 ? "男" : "女");
        } else {
            switchGender();
        }
        PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
        Glide.with(getActivity()).load(Constant.getImgUrl() + instance.getUserLogo())
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.drawable.load_fail_pic))
                .into(wechatIcon);
        if (member.getTimes() > 0) {
            showToast("用户" + member.getName() + "已量体" + member.getTimes() + "次");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (measurePresenter != null) {
            measurePresenter.unsubscribe();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speechSynthesizer != null) {
            speechSynthesizer = null;
        }
    }

    //接受消息的地方(在Android的UI线程中)
    public void onEventMainThread(MessageEvent event) {
        if (event.getNum() == 1) {
            Log.e("ahaha", "fragment====ahahah");
        }
    }

    @OnClick({R.id.save_measure_result, R.id.wechat_gender_edit, R.id.camera_add, R.id.next_person,
            R.id.del_1, R.id.del_2, R.id.del_3,
            R.id.img_1, R.id.img_2, R.id.img_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wechat_gender_edit:
                switchGender();
                break;
            case R.id.save_measure_result:
                handleSaveData();
                break;
            case R.id.next_person:
                measureNextPerson();
                break;
            case R.id.camera_add:
                if (unVisibleView.size() > 0) {
                    capturePic();
                } else {
                    showToast("已拍照三张特体图片");
                }
                break;
            case R.id.del_1:
            case R.id.del_2:
            case R.id.del_3:
                delPic((ImageView) view);
                break;
            default:
                break;
        }
    }

    private void measureNextPerson() {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, 1001);
        btnNext.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void delPic(ImageView view) {
        FrameLayout parent = (FrameLayout) view.getParent();
        ImageView img = (ImageView) parent.getChildAt(0);
        img.setImageDrawable(null);
        view.setVisibility(View.INVISIBLE);
        unVisibleView.add(0, parent);
    }

    private void switchGender() {
        String gender = wechatGender.getText().toString();
        int index = 1;
        if (FEMALE.equals(gender)) {
            index = 2;
        }
        //颜色状态列表
        ColorStateList sl = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}},
                new int[]{getResources().getColor(R.color.c252527), getResources().getColor(R.color.primary)});
        new MaterialDialog.Builder(getActivity())
                .title("更正性别")
                .choiceWidgetColor(sl)
                .titleColor(getResources().getColor(R.color.c252527))
                .items(R.array.genders)
                .contentColor(getResources().getColor(R.color.c252527))
                .itemsCallbackSingleChoice(index - 1, (dialog, itemView, which, text) -> {
                    wechatGender.setText(text);
                    if (measurer instanceof WechatUser) {
                        ((WechatUser) measurer).setGender(MALE.equals(text.toString()) ? 1 : 2);
                    } else if (measurer instanceof ThirdMember) {
                        ((ThirdMember) measurer).setGender(MALE.equals(text.toString()) ? 1 : 2);
                    }
                    return true;
                })
                .backgroundColor(getResources().getColor(R.color.white))
                .positiveText(R.string.sure)
                .show();
    }

    /**
     * 处理提交数据逻辑
     */
    private void handleSaveData() {
        if (unMeasuredList.size() != 0) {
            showToast("量体未完成");
            return;
        }
        List<Part> data = new ArrayList<>();
        try {
            for (int i = 0, count = gridView.getCount(); i < count; i++) {
                LinearLayout layout = (LinearLayout) gridView.getChildAt(i);
                MyTextView textV = (MyTextView) layout.getChildAt(0);
                if (textV.getState() == MeasureStateEnum.UNMEASUED.ordinal()) {
                    showToast(textV.getText().toString() + "部位未完成测量");
                    return;
                }
                String cn = textV.getText().toString();
                TextView valueTv = (TextView) layout.getChildAt(2);
                float value = Float.parseFloat(valueTv.getText().toString());
                String s = ((EditText) ((LinearLayout) layout.getChildAt(4)).getChildAt(1)).getText().toString();
                Float offset;
                if (!TextUtils.isEmpty(s)) {
                    offset = Float.parseFloat(s);
                } else {
                    offset = 0.0f;
                }
                Part part = new Part(cn, value, offset);
                data.add(part);
            }

            //自由量体，合同id固定为10000
            String cid = PreferencesUtils.getInstance(getActivity()).getMeasureCid();

            Measurement measurement = new Measurement(measurer, data, cid);
            MultipartBody.Part[] imgs = new MultipartBody.Part[3];
            if (img1.getDrawable() != null) {
                imgs[0] = getSpecialBodyTypePic((String) img1.getTag());
            }
            if (img2.getDrawable() != null) {
                imgs[1] = getSpecialBodyTypePic((String) img2.getTag());
            }
            if (img3.getDrawable() != null) {
                imgs[2] = getSpecialBodyTypePic((String) img3.getTag());
            }

            measurePresenter.saveMeasurement(measurement, imgs);
        } catch (Exception e) {
            e.printStackTrace();
            Gog.e(e.getMessage());
        }
    }

    /**
     * 读取特体大图
     *
     * @param filename
     * @return
     */
    private MultipartBody.Part getSpecialBodyTypePic(String filename) {
        File f = new File(PATH + File.separator + "gtImage" + File.separator + filename + JPG_SUFFIX);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData("img[]", filename, requestFile);
    }

    private void initSpeech() {
        try {
            if (speechSynthesizer == null) {
                speechSynthesizer = new SpeechSynthesizer(getActivity(), Constant.APP_KEY, Constant.APP_SECRET);
            }
            speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
            speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
            speechSynthesizer.init(null);
        } catch (Exception e) {
            LogUtils.fixBug("语音播报出现异常，异常原因: " + LogUtils.getStackMsg(e));
        }
    }

    /**
     * 初始化测量部位列表
     *
     * @param angleList 角度部位集合
     */
    private void initMeasureItemList(List<String> angleList) {
        //初始化所有测量部位
        String items = PreferencesUtils.getInstance(getActivity()).getMeasureItems();
        if (!TextUtils.isEmpty(items)) {
            measureSequence = items.split(",");
        }
        partList.clear();
        if (measureSequence != null && measureSequence.length != 0) {
            for (String name : measureSequence) {
                if (angleList.contains(name)) {
                    partList.add(new Part(name, true));
                } else {
                    partList.add(new Part(name, false));
                }
            }
        } else {
            //未设置默认量体项目，则量体项目为预设的24项量体项目
            List<String> list = Arrays.asList(getResources().getStringArray(R.array.items_sequence));
            for (String name : list) {
                if (angleList.contains(name)) {
                    partList.add(new Part(name, true));
                } else {
                    partList.add(new Part(name, false));
                }
            }
        }
    }

    @Override
    public void onHandleMeasureError(Throwable e) {
        if (e instanceof BleGattException) {
            measurePresenter.reConnect();
        } else {
            onHandleError(e);
        }
    }

    private void initUnMeasureList() {
        int count = gridView.getChildCount();
        unMeasuredList.clear();
        try {
            for (int i = 0; i < count; i++) {
                LinearLayout layout = (LinearLayout) gridView.getChildAt(i);
                MyTextView textView = (MyTextView) layout.getChildAt(0);
                if (textView.getState() == MeasureStateEnum.UNMEASUED.ordinal()) {
                    unMeasuredList.add(layout);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUmMeasureListFlag = false;
    }

    @Override
    public void handleMeasureData(float length, float angle, int battery) {
        MenuItem item = toolbar.getMenu().getItem(0);
        if (battery < BATTERY_LOW) {
            item.setIcon(R.mipmap.battery_low);
        }
        if (battery >= BATTERY_LOW && battery < BATTERY_HIGH) {
            item.setIcon(R.mipmap.battery_mid);
        }
        if (battery >= BATTERY_HIGH) {
            item.setIcon(R.mipmap.battery_high);
        }
        if (initUmMeasureListFlag) {
            initUnMeasureList();
        }
        if (modifyingView != null) {
            assignValue(length, angle, modifyingView, 1);
        } else {
            if (unMeasuredList.size() != 0) {
                assignValue(length, angle, unMeasuredList.get(0), 0);
            } else {
                //无未测项目，提示测量完成，
                showToast(getString(R.string.measure_completed));
            }
        }
    }

    @Override
    public void onSaveSuccess(Result result) {
        showToast("保存成功");
        unmeasuredItemHint.setText("保存成功");
        if (!"10000".equals(result.getId())) {

            int mal = result.getMal();
            int mno = result.getMno();

            measureStatAl.setText(String.valueOf(mal));
            measureStatNo.setText(String.valueOf(mno));

            PreferencesUtils instance = PreferencesUtils.getInstance(getActivity());
            instance.setMeasureNum(mno);
            instance.setMeasureMeasured(mal);
        }
        //清除所有已测量项目
        clearAndMeasureNext();
        btnSave.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void clearAndMeasureNext() {
        //清空量体人信息
        clearPreMeasureUserInfo();
        int count = gridView.getChildCount();
        for (int i = 0; i < count; i++) {
            LinearLayout linearLayout = (LinearLayout) gridView.getChildAt(i);
            MyTextView textTv = (MyTextView) linearLayout.getChildAt(0);
            ((TextView) linearLayout.getChildAt(2)).setText(null);
            ((EditText) ((LinearLayout) linearLayout.getChildAt(4)).getChildAt(1)).setText(null);
            textTv.setState(MeasureStateEnum.UNMEASUED.ordinal());
            textTv.setTextColor(getResources().getColor(R.color.unmeasured));
        }
        frame1.getChildAt(1).setVisibility(View.INVISIBLE);
        frame2.getChildAt(1).setVisibility(View.INVISIBLE);
        frame3.getChildAt(1).setVisibility(View.INVISIBLE);
        img1.setImageDrawable(null);
        img2.setImageDrawable(null);
        img3.setImageDrawable(null);
    }

    private void clearPreMeasureUserInfo() {
        wechatGender.setText("N/A");
        wechatIcon.setBackground(getResources().getDrawable(R.drawable.load_fail_pic));
        wechatNickname.setText("N/A");
    }

    @Override
    public void showSaveError(Throwable e) {
        showLoading(false);
        onHandleMeasureError(e);
    }

    @Override
    public void showSaveCompleted() {
        showLoading(false);
    }

    @Override
    public void showLoading(boolean bool) {
        if (bool) {
            saveProgressbar = new MaterialDialog.Builder(getActivity())
                    .progress(true, 100)
                    .backgroundColor(getResources().getColor(R.color.white))
                    .show();
        } else {
            saveProgressbar.dismiss();
        }
    }

    @Override
    public void onGetWechatUserInfoSuccess(WechatUser u) {
        showLoading(false);
        measurer = u;
        setWechatUserInfo(u);
    }

    @Override
    public void onShowGetWechatUserInfoError(Throwable e) {
        showLoading(false);
        if (e instanceof ApiException) {
            showToast(e.getMessage());
            clearPreMeasureUserInfo();
            btnSave.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        } else {
            onHandleMeasureError(e);
        }
    }

    @Override
    public void showCompleteGetInfo() {
        showLoading(false);
    }

    @Override
    public void onShowDevicePrepareConnectionError() {
        showToast("蓝牙连接失败，请重新连接");
    }

    @Override
    public void onHandleMeasureError() {
        speechSynthesizer.playText(getString(R.string.measure_error));
    }

    /**
     * 结果赋值，有几个字段需要的结果为角度
     *
     * @param length
     * @param angle
     * @param currentItemView
     * @param type
     */
    private void assignValue(float length, float angle, LinearLayout currentItemView, int type) {
        String cn;
        MyTextView textTv = (MyTextView) currentItemView.getChildAt(0);
        TextView valueTv = (TextView) currentItemView.getChildAt(2);
        try {
            cn = textTv.getText().toString();
            String value;  //播报的测量结果
            if (angleList.contains(cn)) {
                textTv.setValue(angle);
                value = angle + "";
            } else {
                textTv.setValue(length);
                value = length + "";
            }
            valueTv.setText(value);
            textTv.setState(MeasureStateEnum.MEASURED.ordinal());
            textTv.setTextColor(getResources().getColor(R.color.measured));
            String s = null; //最终播放文字
            String result;//播报当前测量结果
            String[] strings = getNextString(type);
            if (type == 1) { //修改原有结果
                result = cn + value;
                modifyingView = null;//重置待修改项
            } else { //按顺序测量
                result = cn + value;
                unMeasuredList.remove(0);// 最前的一项测量完毕
            }
            if (!TextUtils.isEmpty(strings[0])) {
                s = result + "        请测" + strings[0];
                unmeasuredItemHint.setText(strings[0]);
            }
            if (!TextUtils.isEmpty(strings[1])) {
                s = result + strings[1];
                unmeasuredItemHint.setText(strings[1]);
            }
            speechSynthesizer.playText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getNextString(int type) {
        String last = null;
        String next = null;
        String[] strings = new String[2];
        if (type == 1) {
            if (unMeasuredList.size() == 0) {
                last = "测量完毕";
            } else {
                next = ((MyTextView) unMeasuredList.get(0).getChildAt(0)).getText().toString();
            }
        } else {
            if (unMeasuredList.size() == 1) {
                last = "测量完毕";
            } else {
                next = ((MyTextView) unMeasuredList.get(1).getChildAt(0)).getText().toString();
            }
        }
        strings[0] = next;
        strings[1] = last;
        return strings;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoCrop(data.getData());
                    break;
                case TAKE_PHOTO:
                    startPhotoCrop(imageUri);
                    //广播刷新相册
                    Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intentBc.setData(imageUri);
                    getActivity().sendBroadcast(intentBc);
                    break;
                case DISPLAY_PHOTO:
                    //广播刷新相册
                    Intent intentBc1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intentBc1.setData(imageUri);
                    getActivity().sendBroadcast(intentBc1);
                    Bitmap bitmap = BitmapUtils.decodeUri(getActivity(), imageUri, 800, 800);
                    FrameLayout frameLayout = unVisibleView.get(0);
                    ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        frameLayout.getChildAt(1).setVisibility(View.VISIBLE);
                        unVisibleView.remove(0);
                        if (!TextUtils.isEmpty(picName)) {
                            imageView.setTag(picName);
                        }
                    } else {
                        showToast("拍照失败");
                    }
                    break;
                case SCAN_HINT:
                    if (data == null) {
                        //未扫描获取到数据或者拍照中错误返回
                        showToast(getString(R.string.scan_qrcode_failed));
                        btnSave.setVisibility(View.GONE);
                        btnNext.setVisibility(View.VISIBLE);
                        return;
                    }
                    Bundle scan_bundle = data.getExtras();
                    String result = scan_bundle.getString("result");
                    if (result != null) {
                        if (result.contains("https")) {
                            //解析出tid(ThirdMember中id)
                            int redirectUriIndex = result.indexOf(REDIRECT_URI) + REDIRECT_URI.length() + 1;
                            String s = result.substring(redirectUriIndex);
                            try {
                                String tid = LogUtils.getParams(s, "tid");
                                String cid = PreferencesUtils.getInstance(getActivity()).getMeasureCid();
                                measurePresenter.getThirdMemberInfo(tid, cid);
                            } catch (Exception e) {
                                showToast("二维码解析失败，请重试");
                                return;
                            }
                        } else {
                            measurePresenter.getUserInfoWithOpenID(result);
                        }
                    } else {
                        showToast(getString(R.string.scan_qrcode_failed));
                    }
                    break;
                case CODE_HINT:
                    if (data == null) {
                        return;
                    }
                    Bundle code_bundle = data.getExtras();
                    String code = code_bundle.getString("result");
                    if (code != null) {
                        measurePresenter.getUserInfoWithCode(code);
                    } else {
                        showToast(getString(R.string.enter_qrcode_error));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void capturePic() {
        Date date = new Date(System.nanoTime());
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(date.toString().getBytes());
            picName = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            picName = date.toString();
            e.printStackTrace();
        }
        File storageFile = new File(PATH.getAbsoluteFile() + File.separator + "gtImage");
        if (!storageFile.isDirectory()) {
            storageFile.mkdirs();
        }
        File outputImage = new File(storageFile, picName + JPG_SUFFIX);
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            Gog.e(e.toString());
        }

        //将File对象转换为Uri并启动照相程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // 系统版本大于N的统一用FileProvider处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 将文件转换成content://Uri的形式
            imageUri = FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".fileprovider", outputImage);
            // 申请临时访问权限
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } else {
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            imageUri = Uri.fromFile(outputImage);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(intent, DISPLAY_PHOTO);
    }

    private void startPhotoCrop(Uri imageUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("scale", true);
        //设置宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置裁剪图片宽高
        intent.putExtra("outputX", 800);
        intent.putExtra("outputY", 800);
        intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO); //设置裁剪参数显示图片至ImageView
    }

    @Override
    protected void toast2Speech(String s) {
        speechSynthesizer.playText(s);
    }

    /**
     * 不使用第三方库提供的按两下返回键退出机制
     * 防止量体过程中不小心按到返回键
     *
     * @return
     */
    @Override
    public boolean onBackPressedSupport() {
        showHandleBackPress();
        return true;
    }

    private void showHandleBackPress() {
        new MaterialDialog.Builder(getActivity())
                .title("确定要离开当前量体界面?")
                .onPositive((d, i) -> {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                })
                .positiveText(getResources().getString(R.string.sure))
                .negativeText("点错了")
                .show();
    }

    @Override
    public void onGetThirdMemberInfo(ThirdMember user) {
        showLoading(false);
        measurer = user;
        setThirdMemberInfo(user);
    }

    @Override
    public void showGetThirdMemberInfoError(Throwable e) {
        showLoading(false);
        if (e instanceof ApiException) {
            showToast(e.getMessage());
            clearPreMeasureUserInfo();
            btnSave.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        } else {
            onHandleMeasureError(e);
        }
    }

    @Override
    public void showCompleteGetThirdMemberInfo() {
        showLoading(false);
    }
}
package com.npclo.imeasurer.measure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.camera.CaptureActivity;
import com.npclo.imeasurer.data.measure.Measurement;
import com.npclo.imeasurer.data.measure.Part;
import com.npclo.imeasurer.data.wuser.WechatUser;
import com.npclo.imeasurer.main.MainActivity;
import com.npclo.imeasurer.utils.BitmapUtils;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.Gog;
import com.npclo.imeasurer.utils.LogUtils;
import com.npclo.imeasurer.utils.MeasureStateEnum;
import com.npclo.imeasurer.utils.views.MyGridView;
import com.npclo.imeasurer.utils.views.MyTextView;
import com.polidea.rxandroidble.exceptions.BleGattException;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class MeasureFragment extends BaseFragment implements MeasureContract.View {
    public static final int BATTERY_LOW = 30;
    public static final int BATTERY_HIGH = 80;
    public static final String FEMALE = "女";
    public static final String MALE = "男";
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
    private MeasureContract.Presenter measurePresenter;
    private WechatUser user;
    private SpeechSynthesizer speechSynthesizer;
    private MaterialDialog saveProgressbar;
    public static final int TAKE_PHOTO = 13;
    public static final int CROP_PHOTO = 14;
    private static final int IMAGE_REQUEST_CODE = 15;
    public static final int DISPLAY_PHOTO = 16;
    private List<FrameLayout> unVisibleView = new ArrayList<>();
    private List<MyTextView> unMeasuredList = new ArrayList<>();
    private List<String> angleList;
    private List<Part> partList = new ArrayList<>();
    private MyTextView modifyingView;
    private boolean initUmMeasureListFlag;
    private Uri imageUri;
    private PopupWindow popupWindow;
    private AppCompatTextView popupContentTv;
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private String[] measureSequence;

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
        //渲染测量部位列表
        initMeasureItemList();
        ItemAdapter adapter = new ItemAdapter(getActivity(), R.layout.list_measure_item, (ArrayList<Part>) partList);
        gridView.setAdapter(adapter);// TODO: 2017/9/4 使用RecyclerView替代
        // FIXME: 2017/9/8 notifyItemChanged 部分绑定
        gridView.setOnItemClickListener((AdapterView<?> var1, View view, int position, long var4) -> {
            resetTextViewClickState();
            MyTextView textView = (MyTextView) ((LinearLayout) view).getChildAt(0);
            String cn = textView.getText().toString();
            //只有处于已测量的部位才能修改，未测量部位不能修改
            if (textView.getState() == MeasureStateEnum.MEASURED.ordinal()) {
                textView.setTextColor(getResources().getColor(R.color.modifying));
                textView.setState(MeasureStateEnum.MODIFYING.ordinal());
                popupContentTv.setText(cn);//设置当前修改部位弹窗显示   // FIXME: 2017/10/17 下一个测量弹窗不显示
                speechSynthesizer.playText("重新测量部位" + cn);
                modifyingView = textView;
            }
        });
        unVisibleView.clear();
        unVisibleView.add(frame1);
        unVisibleView.add(frame2);
        unVisibleView.add(frame3);
        //初始化需要测量角度的部位
        String[] angleItems = getResources().getStringArray(R.array.angle_items);
        angleList = Arrays.asList(angleItems);
        //初始化所有测量部位
        measureSequence = getResources().getStringArray(R.array.items_sequence);

        initToolbar();
        //初始化语音播报
        initSpeech();
    }

    private void initToolbar() {
        toolbar.setTitle("量体");
        toolbar.setNavigationIcon(R.mipmap.left);
        toolbar.setNavigationOnClickListener(v -> onHandleBackPress());
        toolbar.inflateMenu(R.menu.base_toolbar_menu);
        toolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_unknown);
    }

    private void initPopupWindow() {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(getActivity());
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            View popupContent = LayoutInflater.from(getActivity()).inflate(R.layout.view_popupwindow, null);
            popupWindow.setContentView(popupContent);
            popupContentTv = (AppCompatTextView) popupContent.findViewById(R.id.tv_item);
            popupContentTv.setTextColor(getResources().getColor(R.color.ff0000));
        }
        popupWindow.setFocusable(false);
        popupWindow.showAtLocation(mRootView, Gravity.CENTER, 0, 0);
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
        if (measurePresenter != null) {
            measurePresenter.subscribe();
        }

        user = getActivity().getIntent().getBundleExtra("userBundle").getParcelable("user");
        //仅接收homefragment传值过来的用户信息时才赋值，从当前fragment发起的意图返回结果不在此处进行赋值调用
        if (user != null) {
            setWechatUserInfo(user);
        }

        initUmMeasureListFlag = true;
    }

    private void setWechatUserInfo(WechatUser u) {
        wechatNickname.setText(u.getNickname());
        if (u.getGender() != 0) {
            wechatGender.setText(u.getGender() == 1 ? "男" : "女");
        } else {
            switchGender();
        }
        if (u.getAvatar() != null) {
            Glide.with(this).load(u.getAvatar()).into(wechatIcon);
        } else {
            wechatIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (measurePresenter != null) {
            measurePresenter.unsubscribe();
        }
        if (popupWindow != null) {
            popupWindow.dismiss();
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
                capturePic();
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
        parent.setVisibility(View.INVISIBLE);
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
                .title("修改性别")
                .choiceWidgetColor(sl)
                .titleColor(getResources().getColor(R.color.c252527))
                .items(R.array.genders)
                .contentColor(getResources().getColor(R.color.c252527))
                .itemsCallbackSingleChoice(index - 1, (dialog, itemView, which, text) -> {
                    wechatGender.setText(text);
                    user.setGender(MALE.equals(text.toString()) ? 1 : 2);
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
                MyTextView textView = (MyTextView) ((LinearLayout) gridView.getChildAt(i)).getChildAt(0);
                if (textView.getState() == MeasureStateEnum.UNMEASUED.ordinal()) {
                    showToast(textView.getText().toString() + "部位未完成测量");
                    return;
                }
                float value = textView.getValue();
                String cn = textView.getText().toString();
                Part part = new Part(cn, value);
                data.add(part);
            }

            SharedPreferences sharedPreferences = getActivity()
                    .getSharedPreferences(getString(R.string.app_name), Context.MODE_APPEND);
            String cid = sharedPreferences.getString("id", "");
            String oid = sharedPreferences.getString("orgId", "");

            Measurement measurement = new Measurement(user, data, cid, oid);
            MultipartBody.Part[] imgs = new MultipartBody.Part[3];
            if (img1.getDrawable() != null) {
                imgs[0] = drawable2file(img1, "img1");
            }
            if (img2.getDrawable() != null) {
                imgs[1] = drawable2file(img2, "img2");
            }
            if (img3.getDrawable() != null) {
                imgs[2] = drawable2file(img3, "img3");
            }

            measurePresenter.saveMeasurement(measurement, imgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MultipartBody.Part drawable2file(ImageView img, String filename) throws IOException {
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        File f = new File(getContext().getCacheDir(), filename);
        f.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData("img[]", filename, requestFile);//att 多文件上传名字
    }

    private void initSpeech() {
        if (speechSynthesizer == null) {
            speechSynthesizer = new SpeechSynthesizer(getActivity(), Constant.APP_KEY, Constant.APP_SECRET);
        }
        speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
        speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
        speechSynthesizer.init(null);
    }

    private void initMeasureItemList() {
        String[] strings = getResources().getStringArray(R.array.items_sequence);
        for (String name : strings) {
            partList.add(new Part(name));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String s, s2;
        if (unMeasuredList.size() > 0) {
            s2 = "当前测量部位";
            s = unMeasuredList.get(0).getText().toString();
        } else {
            s2 = "请确定待测人员性别，首先测量部位";
            // FIXME: 13/12/2017 语音播报生命周期
            if (measureSequence == null) {
                s = partList.get(0).getCn();
            } else {
                s = measureSequence[0];
            }
        }
        speechSynthesizer.playText(s2 + s);
        if (popupContentTv != null) {
            popupContentTv.setText(s);
            //更新当前测量部位弹窗显示
        }
    }

    @Override
    public void onHandleMeasureError(Throwable e) {
        Gog.e("出错了======" + e.toString());
        if (e instanceof BleGattException) {
            toast2Speech("蓝牙连接断开");
            measurePresenter.reConnect();
            LogUtils.fixBug("蓝牙断开=>" + e.toString());
        } else {
            super.onHandleError(e);
        }
    }

    @Override
    public void showPopWindow() {
        initPopupWindow();
    }

    private void showReConnectDialog() {
        new MaterialDialog.Builder(getActivity())
                .contentColor(getResources().getColor(R.color.primary))
                .backgroundColor(getResources().getColor(R.color.white))
                .content("蓝牙连接断开，重新连接蓝牙？")
                .positiveText("确定")
                .negativeText("取消")
                .positiveColor(getResources().getColor(R.color.ff5001))
                .negativeColor(getResources().getColor(R.color.c252527))
                .onPositive((dialog, which) -> measurePresenter.reConnect())
                .show();
    }

    private void initUnMeasureList() {
        int count = gridView.getChildCount();
        unMeasuredList.clear();
        try {
            for (int i = 0; i < count; i++) {
                MyTextView textView = (MyTextView) ((LinearLayout) gridView.getChildAt(i)).getChildAt(0);
                if (textView.getState() == MeasureStateEnum.UNMEASUED.ordinal()) {
                    unMeasuredList.add(textView);
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
        //att 先判断是否有正处于修改状态的textview，有的话，先给其赋值，再给下一个未测量的部位赋值
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
    public void showSuccessSave() {
        showToast("保存成功");
        popupContentTv.setText("保存成功");
        //清除所有已测量项目
        clearAndMeasureNext();
        btnSave.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void clearAndMeasureNext() {
        int count = gridView.getChildCount();
        for (int i = 0; i < count; i++) {
            LinearLayout linearLayout = (LinearLayout) gridView.getChildAt(i);
            MyTextView textView = (MyTextView) linearLayout.getChildAt(0);
            textView.setState(MeasureStateEnum.UNMEASUED.ordinal());
            textView.setTextColor(getResources().getColor(R.color.unmeasured));
            textView.setValue(0.0f);
        }
        frame1.setVisibility(View.INVISIBLE);
        frame2.setVisibility(View.INVISIBLE);
        frame3.setVisibility(View.INVISIBLE);
        img1.setImageDrawable(null);
        img2.setImageDrawable(null);
        img3.setImageDrawable(null);
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
        user = u;
        setWechatUserInfo(user);
    }

    @Override
    public void showGetInfoError(Throwable e) {
        showLoading(false);
        onHandleMeasureError(e);
    }

    @Override
    public void showCompleteGetInfo() {
        showLoading(false);
    }

    @Override
    public void onShowDevicePrepareConnectionError() {
        showToast("蓝牙状态异常，请重新连接");
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
     * @param textView
     * @param type
     */
    private void assignValue(float length, float angle, MyTextView textView, int type) {
        String cn = textView.getText().toString();
        try {
            String value;  //播报的测量结果
            if (angleList.contains(cn)) {
                textView.setValue(angle);
                value = angle + "";
            } else {
                textView.setValue(length);
                value = length + "";
            }
            textView.setState(MeasureStateEnum.MEASURED.ordinal());
            textView.setTextColor(getResources().getColor(R.color.measured));
            String s = null;//最终播放文字
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
                popupContentTv.setText(strings[0]);
            }
            if (!TextUtils.isEmpty(strings[1])) {
                s = result + strings[1];
                popupContentTv.setText(strings[1]);
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
                next = unMeasuredList.get(0).getText().toString();
            }
        } else {
            if (unMeasuredList.size() == 1) {
                last = "测量完毕";
            } else {
                next = unMeasuredList.get(1).getText().toString();
            }
        }
        strings[0] = next;
        strings[1] = last;
        return strings;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String uid = getActivity().getSharedPreferences(getString(R.string.app_name),
                Context.MODE_APPEND).getString("id", null);
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
            case CROP_PHOTO:
                try {
                    if (data != null) {
                        // FIXME: 11/12/2017 获取大图（根据图片文件名获取大图）
                        Bundle bundle = data.getExtras();
                        Bitmap bitmap = bundle.getParcelable("data");
                        FrameLayout frameLayout = unVisibleView.get(0);
                        ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                        Matrix matrix = new Matrix();
                        matrix.setScale(0.5f, 0.5f);
                        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                bitmap.getHeight(), matrix, true);
                        bitmap.recycle();
                        imageView.setImageBitmap(bm);
                        frameLayout.getChildAt(1).setVisibility(View.VISIBLE);
                        unVisibleView.remove(0);
                    }
                } catch (Exception e) {
                    showToast("操作失败，请重试");
                    e.printStackTrace();
                }
                break;
            case DISPLAY_PHOTO:
                //广播刷新相册
                Intent intentBc1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentBc1.setData(imageUri);
                getActivity().sendBroadcast(intentBc1);
                Bitmap bitmap = BitmapUtils.decodeUri(getActivity(), imageUri, 800, 800);//att 获得小预览图
                FrameLayout frameLayout = unVisibleView.get(0); // FIXME: 14/12/2017 出现数组越界问题
                ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    frameLayout.setVisibility(View.VISIBLE);
                    unVisibleView.remove(0);
                }
                break;
            case SCAN_HINT:
                String id = null;
                try {
                    Bundle bundle = data.getExtras();
                    id = bundle.getString("result");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (id != null) {
                    measurePresenter.getUserInfoWithOpenID(id, uid);
                } else {
                    showToast(getString(R.string.scan_qrcode_failed));
                }
                break;
            case CODE_HINT:
                String code = null;
                try {
                    Bundle bundle = data.getExtras();
                    code = bundle.getString("result");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (code != null) {
                    measurePresenter.getUserInfoWithCode(code, uid);
                } else {
                    showToast(getString(R.string.enter_qrcode_error));
                }
                break;
            default:
                break;
        }
    }

    private void capturePic() {
        Date date = new Date(System.nanoTime());
        String picName;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(date.toString().getBytes());
            picName = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            picName = date.toString();
            e.printStackTrace();
        }
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, picName + ".jpg");
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将File对象转换为Uri并启动照相程序
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
        startActivityForResult(intent, DISPLAY_PHOTO); //启动照相
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
        startActivityForResult(intent, CROP_PHOTO);
        // 设置裁剪参数显示图片至ImageView
    }

    @Override
    protected void toast2Speech(String s) {
        speechSynthesizer.playText(s);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        speechSynthesizer = null;
    }

    @Override
    public boolean onBackPressedSupport() {
        onHandleBackPress();
        return true;
    }

    private void onHandleBackPress() {
        new MaterialDialog.Builder(getActivity())
                .title("确定要离开当前量体界面?")
                .onPositive((d, i) -> startActivity(new Intent(getActivity(), MainActivity.class)))
                .positiveText(getResources().getString(R.string.sure))
                .negativeText("点错了")
                .show();
    }
}
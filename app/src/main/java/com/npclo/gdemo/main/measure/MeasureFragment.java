package com.npclo.gdemo.main.measure;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.gdemo.main.home.HomeFragment;
import com.npclo.gdemo.main.home.HomePresenter;
import com.npclo.gdemo.utils.BitmapUtils;
import com.npclo.gdemo.utils.MeasureStateEnum;
import com.npclo.gdemo.utils.schedulers.SchedulerProvider;
import com.npclo.gdemo.utils.views.MyGridView;
import com.npclo.gdemo.utils.views.MyTextView;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class MeasureFragment extends BaseFragment implements MeasureContract.View {
    private static final String TAG = MeasureFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_CAPTURE = 101;
    private static final int MY_PERMISSIONS_REQUEST_CHOOSE = 102;
    @BindView(R.id.base_toolbar)
    Toolbar baseToolbar;
    @BindView(R.id.wechat_icon)
    ImageView wechatIcon;
    @BindView(R.id.wechat_nickname)
    TextView wechatNickname;
    //    @BindView(R.id.wechat_name)
//    TextView wechatName;
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
    LinearLayout gender_line;
    @BindView(R.id.img_1)
    ImageView img_1;
    @BindView(R.id.img_2)
    ImageView img_2;
    @BindView(R.id.img_3)
    ImageView img_3;
    @BindView(R.id.del_1)
    ImageView del_1;
    @BindView(R.id.del_2)
    ImageView del_2;
    @BindView(R.id.del_3)
    ImageView del_3;
    @BindView(R.id.frame_1)
    FrameLayout frame_1;
    @BindView(R.id.frame_2)
    FrameLayout frame_2;
    @BindView(R.id.frame_3)
    FrameLayout frame_3;
    Unbinder unbinder;
    @BindView(R.id.user_layout)
    LinearLayout userLayout;
    @BindView(R.id.imageView2)
    ImageView imageView2;
    private MeasureContract.Presenter measurePresenter;
    private SpeechSynthesizer speechSynthesizer;
    private MaterialDialog saveProgressbar;
    public static final int TAKE_PHOTO = 13;
    public static final int CROP_PHOTO = 14;
    private static final int IMAGE_REQUEST_CODE = 15;
    public static final int DISPLAY_PHOTO = 16;
    private List<FrameLayout> unVisibleView = new ArrayList<>();
    private List<MyTextView> unMeasuredList = new ArrayList<>();
    private List<String> angleList;
    private MyTextView modifyingView;
    private boolean initUmMeasureListFlag;
    private Uri imageUri;
    private boolean firstHint = true;
    private PopupWindow popupWindow;
    private AppCompatTextView popup_content_tv;

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
    protected void afterCreate(Bundle savedInstanceState) {
        super.afterCreate(savedInstanceState);
        Bundle bundle = getArguments();
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        initToolbar();
        //渲染测量部位列表
        initMeasureItemList();
//        ItemAdapter adapter = new ItemAdapter(getActivity(), R.layout.item_qc, (ArrayList<Part>) partList);
//        gridView.setAdapter(adapter);// TODO: 2017/9/4 使用RecyclerView替代
        // FIXME: 2017/9/8 notifyItemChanged 部分绑定
        gridView.setOnItemClickListener((AdapterView<?> var1, View view, int position, long var4) -> {
            resetTextViewClickState();
            MyTextView textView = (MyTextView) ((LinearLayout) view).getChildAt(0);
            String cn = textView.getText().toString();
            //att 只有处于已测量的部位才能修改，未测量部位不能修改
            if (textView.getState() == MeasureStateEnum.MEASURED.ordinal()) {
                textView.setTextColor(getResources().getColor(R.color.modifying));
                textView.setState(MeasureStateEnum.MODIFYING.ordinal());
                popup_content_tv.setText(cn);//设置当前修改部位弹窗显示
                speechSynthesizer.playText("重新测量部位" + cn);
                modifyingView = textView;//att 正在修改测量值textview赋值
            }
        });
        unVisibleView.clear();
        unVisibleView.add(frame_1);
        unVisibleView.add(frame_2);
        unVisibleView.add(frame_3);

    }

    // FIXME: 2017/9/8 遍历 更好的方式
    //att 针对误点击进行修改操作，重置所有的处于修改状态的textview为非修改状态
    private void resetTextViewClickState() {
        for (int i = 0, count = gridView.getChildCount(); i < count; i++) {
            MyTextView textView = (MyTextView) ((LinearLayout) gridView.getChildAt(i)).getChildAt(0);
            if (textView.getState() == MeasureStateEnum.MODIFYING.ordinal()) {
                textView.setState(MeasureStateEnum.MEASURED.ordinal());
                textView.setTextColor(getResources().getColor(R.color.measured));
            }
        }
    }

    private void initToolbar() {
        baseToolbar.setNavigationIcon(R.mipmap.left);
        baseToolbar.setNavigationOnClickListener(__ -> {
            HomeFragment homeFragment = HomeFragment.newInstance();
            start(homeFragment, SINGLETASK);
            homeFragment.setPresenter(new HomePresenter(((MainActivity) getActivity()).getRxBleClient(), homeFragment, SchedulerProvider.getInstance()));
        });
        baseToolbar.inflateMenu(R.menu.base_toolbar_menu);
        baseToolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_unknown);
    }

    @Override
    public void onResume() {
        super.onResume();
        initSpeech();
        measurePresenter.subscribe();
        String[] angleItems = getResources().getStringArray(R.array.angle_items);
        angleList = Arrays.asList(angleItems);
        try {
            MainActivity activity = ((MainActivity) getActivity());
            RxBleDevice bleDevice = activity.getRxBleDevice();
            if (bleDevice != null && bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
                //启动测量

                UUID characteristicUUID = activity.getCharacteristicUUID();
                Observable<RxBleConnection> connectionObservable = activity.getConnectionObservable();
                measurePresenter.startMeasure(characteristicUUID, connectionObservable);
            }
        } catch (Exception e) {
            showToast("蓝牙连接异常，请重新连接！");
            e.printStackTrace();
        }
        initUmMeasureListFlag = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        measurePresenter.unsubscribe();
        popupWindow.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStop() {
        super.onStop();
        speechSynthesizer = null;
    }

    @OnClick({R.id.save_measure_result, R.id.wechat_gender_edit, R.id.camera_add, R.id.next_person,
            R.id.del_1, R.id.del_2, R.id.del_3,
            R.id.img_1, R.id.img_2, R.id.img_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.camera_add:
                capturePic();
                break;
        }
    }

    private void initSpeech() {
        String APPKEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
        String APPSECRET = "29aa998c451d64d9334269546a4021b8";
        if (speechSynthesizer == null)
            speechSynthesizer = new SpeechSynthesizer(getActivity(), APPKEY, APPSECRET);
        speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
        speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
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

    private void initMeasureItemList() {
//        try {
//            MeasurementItem item = (MeasurementItem) Class.forName(ITEM_PACKAGE + ".MeasurementItem").newInstance();
//            Field[] declaredFields = item.getClass().getDeclaredFields();
//            List<String> nameList = new ArrayList<>();
//            for (Field field : declaredFields) {
//                String name = field.getName();
//                nameList.add(name);
//            }
//
//            String[] objects = new String[nameList.size()];
//            String[] strings = nameList.toArray(objects);
//            Arrays.sort(strings);
//            //att 循环添加单行
//            for (String name : strings) {
//                Class<?> itemSubclass = Class.forName(PART_PACKAGE + "." + name);
//                Part part = (Part) itemSubclass.newInstance();
//                partList.add(new Part(part.getCn(), part.getEn()));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void handleError(Throwable e) {
        handleError(e, TAG);
    }

    @Override
    public void showStartReceiveData() {
    }

    @Override
    public void bleDeviceMeasuring() {
        String[] measureSequence = getResources().getStringArray(R.array.items_sequence);
        if (firstHint) {
            speechSynthesizer.playText("请确定待测人员性别，首先测量部位" + measureSequence[0]);
            popup_content_tv.setText(measureSequence[0]);//更新当前测量部位弹窗显示
            firstHint = false;
        }
    }

    private void initUnMeasureList() {
        int count = gridView.getChildCount();
        unMeasuredList.clear();
        try {
            for (int i = 0; i < count; i++) {
                MyTextView textView = (MyTextView) ((LinearLayout) gridView.getChildAt(i)).getChildAt(0);
                if (textView.getState() == MeasureStateEnum.UNMEASUED.ordinal())
                    unMeasuredList.add(textView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUmMeasureListFlag = false;
    }

    @Override
    public void handleMeasureData(float length, float angle, int battery) {
        if (battery < 30) baseToolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_low);
        if (battery >= 30 && battery < 80)
            baseToolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_mid);
        if (battery >= 80) baseToolbar.getMenu().getItem(0).setIcon(R.mipmap.battery_high);
        if (initUmMeasureListFlag) initUnMeasureList();
        //att 先判断是否有正处于修改状态的textview，有的话，先给其赋值，再给下一个未测量的部位赋值
        if (modifyingView != null) {
            assignValue(length, angle, modifyingView, 1);
        } else {
            try {
                MyTextView textView = unMeasuredList.get(0);
                if (textView != null) {
                    assignValue(length, angle, textView, 0);
                }
            } catch (Exception e) {
                //无未测项目，提示测量完成，
                showToast(getString(R.string.measure_completed));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showSuccessSave() {
        showToast("保存成功");
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
            textView.setValue("");
        }
        frame_1.setVisibility(View.INVISIBLE);
        frame_2.setVisibility(View.INVISIBLE);
        frame_3.setVisibility(View.INVISIBLE);
        img_1.setImageDrawable(null);
        img_2.setImageDrawable(null);
        img_3.setImageDrawable(null);
    }

    @Override
    public void showSaveError(Throwable e) {
        showLoading(false);
        handleError(e, TAG);
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

    /**
     * 结果赋值，有几个字段需要的结果为角度
     *
     * @param length
     * @param angle
     * @param textView
     * @param type
     */
    private void assignValue(float length, float angle, MyTextView textView, int type) {
        String tag = (String) textView.getTag();
        String cn;
        try {
//            Part part = (Part) Class.forName(PART_PACKAGE + "." + tag).newInstance();
//            cn = part.getCn();
            String value;//播报的测量结果
            if (angleList.contains(tag)) {
                //按要求赋值
                //                if (angle > 90f) {
                //                    speechSynthesizer.playText(cn + "测量结果有误，请重新测量");
                //                    return;
                //                } //att 这块容易出现错误，角度出现不准的情况
                textView.setValue(angle + "");
                value = angle + "";
            } else {
                textView.setValue(length + "");
                value = length + "";
            }
            textView.setState(MeasureStateEnum.MEASURED.ordinal());//更新状态
            textView.setTextColor(getResources().getColor(R.color.measured));//修改颜色
            String s = null;//最终播放文字
            String result;//播报当前测量结果
            String[] strings = getNextString(type);
            if (type == 1) { //修改原有结果
//                result = cn + value;
                modifyingView = null;//att 重置待修改项
            } else { //按顺序测量
//                result = cn + value;
                unMeasuredList.remove(0);//att 最前的一项测量完毕
            }
            if (!TextUtils.isEmpty(strings[0])) {
//                s = result + "        请测" + strings[0];
                popup_content_tv.setText(strings[0]);
            }
            if (!TextUtils.isEmpty(strings[1])) {
//                s = result + strings[1];
                popup_content_tv.setText(strings[1]);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        Bundle bundle = data.getExtras();
                        Bitmap bm = bundle.getParcelable("data");
                        FrameLayout frameLayout = unVisibleView.get(0);
                        ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                        Matrix matrix = new Matrix();// att 裁剪压缩图片
                        matrix.setScale(0.5f, 0.5f);
                        Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                                bm.getHeight(), matrix, true);// FIXME: 2017/9/11 bitmap为空
                        bm.recycle();
                        imageView.setImageBitmap(bm1);
                        frameLayout.setVisibility(View.VISIBLE);
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
                FrameLayout frameLayout = unVisibleView.get(0);
                ImageView imageView = (ImageView) frameLayout.getChildAt(0);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    frameLayout.setVisibility(View.VISIBLE);
                    unVisibleView.remove(0);
                }
                break;
            default:
                break;
        }
    }

    private void capturePic() {
        Date date = new Date(System.nanoTime());
        String pic_name;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(date.toString().getBytes());
            pic_name = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            pic_name = date.toString();
            e.printStackTrace();
        }
        //创建File对象用于存储拍照的图片 SD卡根目录  TODO 判断
        //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
        //存储至DCIM文件夹
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, pic_name + ".jpg");
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
        Intent intent = new Intent("com.android.camera.action.CROP"); //剪裁
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
}
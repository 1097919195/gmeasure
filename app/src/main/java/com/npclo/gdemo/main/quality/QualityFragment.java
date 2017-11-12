package com.npclo.gdemo.main.quality;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseApplication;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.camera.CaptureActivity;
import com.npclo.gdemo.data.quality.Part;
import com.npclo.gdemo.data.quality.QualityItem;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.gdemo.utils.MeasureStateEnum;
import com.npclo.gdemo.utils.views.MyGridView;
import com.npclo.gdemo.utils.views.MyLineLayout;
import com.npclo.gdemo.utils.views.MyTextView;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class QualityFragment extends BaseFragment implements QualityContract.View {
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    public static final int DEVIATION_RANGE = 0;
    @BindView(R.id.de_name)
    AppCompatTextView deName;
    @BindView(R.id.de_battery)
    AppCompatTextView deBattery;
    @BindView(R.id.qu_type)
    AppCompatTextView quType;
    @BindView(R.id.qu_num)
    AppCompatTextView quNum;
    @BindView(R.id.measure_table_layout)
    MyGridView gridView;
    Unbinder unbinder;
    @BindView(R.id.btn_next)
    AppCompatButton btnNext;
    private QualityContract.Presenter mPresenter;
    private boolean initUmMeasureListFlag;
    private List<LinearLayout> unMeasuredList = new ArrayList<>();
    private List<Part> resultPartsList = new ArrayList<>();
    private String itemId;

    @Override
    public void setPresenter(@NonNull QualityContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onStart() {
        super.onStart();
        UUID characteristicUUID = BaseApplication.getUUID(getActivity());
        mPresenter.setUUID(characteristicUUID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_quality;
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
    }

    @Override
    public void onResume() {
        super.onResume();
        initUmMeasureListFlag = true;

        MainActivity activity = ((MainActivity) getActivity());
        String macAddress = BaseApplication.getMacAddress(activity);
        if (!TextUtils.isEmpty(macAddress)) {
            RxBleDevice device = BaseApplication.getRxBleClient(activity).getBleDevice(macAddress);
            if (device != null && device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
                //启动测量
                deName.setText(device.getName());
                mPresenter.subscribe();
            } else {
                deName.setText(getString(R.string.device_disconnected));
                mPresenter.reConnect();
            }
        }

        Bundle bundle = getArguments();
        QualityItem item = bundle.getParcelable("item");
        afterResume(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
        Bundle bundle = getArguments(); // FIXME: 2017/11/12 这一块需要好好考虑。。。
        bundle.putParcelable("item", null);
    }

    private void afterResume(QualityItem item) {
        if (item != null) {
            MainActivity activity = ((MainActivity) getActivity());
            List<Part> partList;
            List<String> partNameList = new ArrayList<>();
            quType.setText(item.getCategory());
            quNum.setText(item.getCode());
            itemId = item.get_id();
            partList = item.getParts();
            initMeasureView(partList);
            if (partList.size() > 0) {
                for (Part p : partList) {
                    partNameList.add(p.getName());
                }
                String name = partNameList.get(0);
                if (activity.speechSynthesizer != null) {
                    activity.speechSynthesizer.playText("请测" + name);
                }
            }
        }
    }

    private void initMeasureView(List<Part> partList) {
        ItemAdapter itemAdapter = new ItemAdapter(getActivity(), R.layout.item_qc, (ArrayList<Part>) partList);
        gridView.setAdapter(itemAdapter);
    }

    public static QualityFragment newInstance() {
        return new QualityFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void handleError(Throwable e) {
        super.handleError(e);
    }

    /**
     * @param length  单位：mm
     * @param angle
     * @param battery
     */
    @Override
    public void handleMeasureData(int length, float angle, int battery) {
        deBattery.setText(battery + "%");
        if (initUmMeasureListFlag) {
            initUnMeasureList();
        }
        if (unMeasuredList.size() != 0) {
            MyLineLayout lineLayout = (MyLineLayout) unMeasuredList.get(0);
            assignValue(length, angle, lineLayout);
        } else {
            //无未测项目，提示测量完成，
            showToast(getString(R.string.measure_completed));
        }
    }

    @Override
    public void handleQualityItemResult(QualityItem qualityItem) {
        afterResume(qualityItem);
    }

    @Override
    public void showDeviceError() {
        showToast("蓝牙状态异常，请重新连接");
    }

    /**
     * 赋值
     *
     * @param length 单位： mm
     * @param angle
     * @param layout
     */
    private void assignValue(int length, float angle, MyLineLayout layout) {
        Part part = new Part();
        MyTextView valueView = ((MyTextView) layout.getChildAt(1)); //赋值
        MyTextView textView = ((MyTextView) layout.getChildAt(0)); //项目
        String cn = (String) textView.getText();
        try {
            float value = (float) length / 10;//显示测量结果: xx cm
            layout.setState(MeasureStateEnum.MEASURED.ordinal());//更新状态
            textView.setTextColor(getResources().getColor(R.color.measured));//修改颜色
            int diff = length - (int) (Float.valueOf(textView.getValue()) * 10);
            valueView.setText(diff + "mm");
            String s = null;//最终播放文字
            String result;//播报当前测量结果
            String[] strings = getNextString();
            result = cn + value + "cm";
            diff = Integer.parseInt(diff + "");
            if (diff > DEVIATION_RANGE) {
                result += "偏大" + diff + "mm";
                part.setCode(1);
            } else if (diff < DEVIATION_RANGE) {
                result += "偏小" + diff + "mm";
                part.setCode(-1);
            } else {
                result += "符合要求";
                part.setCode(0);
            }
            unMeasuredList.remove(0);//att 最前的一项测量完毕
            part.setName(cn);
            part.setOriValue(Float.valueOf(textView.getValue()));
            part.setActValue((float) (length / 10));
            if (!TextUtils.isEmpty(strings[0])) {
                s = result + "        请测" + strings[0];
            }
            if (!TextUtils.isEmpty(strings[1])) {
                s = result + strings[1];
            }
            ((MainActivity) getActivity()).speechSynthesizer.playText(s);
            resultPartsList.add(part);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUnMeasureList() {
        int count = gridView.getChildCount();
        unMeasuredList.clear();
        try {
            for (int i = 0; i < count; i++) {
                MyLineLayout layout = (MyLineLayout) gridView.getChildAt(i);
                if (layout.getState() == MeasureStateEnum.UNMEASUED.ordinal()) {
                    unMeasuredList.add(layout);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUmMeasureListFlag = false;
    }

    private String[] getNextString() {
        String last = null;
        String next = null;
        String[] strings = new String[2];
        if (unMeasuredList.size() == 1) {
            last = "测量完毕";
        } else {
            next = ((MyTextView) unMeasuredList.get(1).getChildAt(0)).getText().toString();
        }
        strings[0] = next;
        strings[1] = last;
        return strings;
    }

    @OnClick(R.id.btn_next)
    public void onViewClicked() {
        if (!initUmMeasureListFlag && unMeasuredList.size() == 0) {
            QualityItem qualityItem = new QualityItem(itemId, resultPartsList);
            mPresenter.uploadResult(qualityItem);
            Intent intent = new Intent(getActivity(), CaptureActivity.class);
            startActivityForResult(intent, 1001);
        } else {
            showToast("未测量完毕");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = null;
        try {
            Bundle bundle = data.getExtras();
            result = bundle.getString("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (resultCode) {
            case SCAN_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithId(result);
                } else {
                    showToast(getString(R.string.scan_qrcode_failed));
                }
                break;
            case CODE_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithCode(result);
                } else {
                    showToast(getString(R.string.enter_qrcode_error));
                }
                break;
            default:
                break;
        }
    }
}
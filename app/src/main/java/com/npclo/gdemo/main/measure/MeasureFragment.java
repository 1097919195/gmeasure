package com.npclo.gdemo.main.measure;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.gdemo.utils.MeasureStateEnum;
import com.npclo.gdemo.utils.views.MyGridView;
import com.npclo.gdemo.utils.views.MyLineLayout;
import com.npclo.gdemo.utils.views.MyTextView;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class MeasureFragment extends BaseFragment implements MeasureContract.View {
    @BindView(R.id.base_toolbar)
    Toolbar baseToolbar;
    @BindView(R.id.de_name)
    AppCompatTextView deName;
    @BindView(R.id.de_battery)
    AppCompatTextView deBattery;
    @BindView(R.id.measure_table_layout)
    MyGridView gridView;
    @BindView(R.id.btn_clear)
    AppCompatButton btnClear;
    Unbinder unbinder;
    private MeasureContract.Presenter measurePresenter;
    private List<MyLineLayout> unMeasuredList = new ArrayList<>();
    private boolean initUmMeasureListFlag;
    private List<String> measure_items;

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
        //渲染测量部位列表
        unbinder = ButterKnife.bind(this, mRootView);
        baseToolbar.inflateMenu(R.menu.base_toolbar_menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        MenuItem item = baseToolbar.getMenu().getItem(0);
        measure_items = Arrays.asList(getResources().getStringArray(R.array.items_sequence));
        try {
            MainActivity activity = ((MainActivity) getActivity());
            RxBleDevice bleDevice = activity.getRxBleDevice();
            if (bleDevice != null && bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
                //启动测量
                deName.setText(bleDevice.getName());
                UUID characteristicUUID = activity.getCharacteristicUUID();
                Observable<RxBleConnection> connectionObservable = activity.getConnectionObservable();
                measurePresenter.startMeasure(characteristicUUID, connectionObservable);
                item.setIcon(R.drawable.ble_connected);
                speech("请测" + measure_items.get(0));
            } else {
                deName.setText(getString(R.string.device_disconnected));
                item.setIcon(R.drawable.ble_disconnected);
                speech("蓝牙未连接");
            }
        } catch (Exception e) {
            showToast("蓝牙连接异常，请重新连接！");
            item.setIcon(R.drawable.ble_disconnected);
            deName.setText("蓝牙状态异常");
        }
        initUmMeasureListFlag = true;
        ItemAdapter2 adapter = new ItemAdapter2(getActivity(), R.layout.item_qc2, measure_items);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        measurePresenter.unsubscribe();
    }

    private void initUnMeasureList() {
        int count = gridView.getChildCount();
        unMeasuredList.clear();
        for (int i = 0; i < count; i++) {
            MyLineLayout layout = (MyLineLayout) gridView.getChildAt(i);
            if (layout.getState() == MeasureStateEnum.UNMEASUED.ordinal())
                unMeasuredList.add(layout);
        }
        initUmMeasureListFlag = false;
    }

    @Override
    public void handleError(Throwable e) {
        super.handleError(e);
    }

    @Override
    public void handleMeasureData(float length, float angle, int battery) {
        if (initUmMeasureListFlag) initUnMeasureList();
        deBattery.setText(battery + "%");
        if (unMeasuredList.size() != 0) {
            MyLineLayout layout = unMeasuredList.get(0);
            assignValue(length, angle, layout);
        } else {
            showToast(getString(R.string.measure_completed));
        }
    }


    private void clearAndMeasureNext() {
        int count = gridView.getChildCount();
        for (int i = 0; i < count; i++) {
            MyLineLayout linearLayout = (MyLineLayout) gridView.getChildAt(i);
            MyTextView textView = (MyTextView) linearLayout.getChildAt(0);
            textView.setState(MeasureStateEnum.UNMEASUED.ordinal());
            textView.setTextColor(getResources().getColor(R.color.unmeasured));
            textView.setValue("");
            ((AppCompatTextView) linearLayout.getChildAt(1)).setText("");
            ((AppCompatTextView) linearLayout.getChildAt(2)).setText("");
        }
        initUmMeasureListFlag = true;
        speech("请测" + measure_items.get(0));
    }

    private void assignValue(float length, float angle, MyLineLayout layout) {
        MyTextView textView = (MyTextView) layout.getChildAt(0);
        AppCompatTextView lengthView = (AppCompatTextView) layout.getChildAt(1);
        AppCompatTextView angleView = (AppCompatTextView) layout.getChildAt(2);
        String cn = textView.getText().toString();
        try {
            String value;//播报的测量结果
            lengthView.setText(length + "cm");
            angleView.setText(angle + "°");
            value = length + "";
            textView.setState(MeasureStateEnum.MEASURED.ordinal());//更新状态
            textView.setTextColor(getResources().getColor(R.color.measured));//修改颜色
            String s = null;//最终播放文字
            String result;//播报当前测量结果
            String[] strings = getNextString();
            result = cn + value;
            unMeasuredList.remove(0);//att 最前的一项测量完毕
            if (!TextUtils.isEmpty(strings[0])) {
                s = result + "        请测" + strings[0];
            }
            if (!TextUtils.isEmpty(strings[1])) {
                s = result + strings[1];
            }
            ((MainActivity) getActivity()).speechSynthesizer.playText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getNextString() {
        String last = null;
        String next = null;
        String[] strings = new String[2];
        if (unMeasuredList.size() == 1) {
            last = "测量完毕";
        } else {
            next = ((MyTextView) (unMeasuredList.get(1)).getChildAt(0)).getText().toString();
        }
        strings[0] = next;
        strings[1] = last;
        return strings;
    }

    @Override
    protected void speech(String s) {
        ((MainActivity) getActivity()).speechSynthesizer.playText(s);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_clear)
    public void onViewClicked() {
        clearAndMeasureNext();
    }
}
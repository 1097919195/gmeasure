package com.npclo.gdemo.main.quality;

import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.data.quality.Part;
import com.npclo.gdemo.data.quality.QualityItem;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.gdemo.main.measure.ItemAdapter;
import com.npclo.gdemo.utils.views.MyGridView;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Endless on 2017/10/13.
 */

public class QualityFragment extends BaseFragment implements QualityContract.View {
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

    @Override
    public void setPresenter(QualityContract.Presenter presenter) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_quality;
    }


    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        MainActivity activity = (MainActivity) getActivity();
        RxBleDevice device = activity.getRxBleDevice();
        if (device != null) {
            deName.setText(device.getName());
        } else {
            deName.setText(getString(R.string.device_disconnected));
        }
        try {
            Bundle bundle = getArguments();
            QualityItem item = bundle.getParcelable("item");
            quType.setText(item.getCategory());
            quNum.setText(item.get_id());
            List<Part> partList = item.getParts();
            initMeasureView(partList);
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                showToast("加载数据失败");
            }
            e.printStackTrace();
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

    public QualityFragment() {
        Bundle bundle = getArguments();
        QualityItem item = bundle.getParcelable("item");

    }
}

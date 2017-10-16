package com.npclo.gdemo.main.home;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.npclo.gdemo.R;
import com.npclo.gdemo.data.BleDevice;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultsAdapter.ViewHolder> {

    private List<BleDevice> mDatas;
    private HomeFragment mFragment;

    public ScanResultsAdapter(@NonNull HomeFragment fragment, @NonNull List<BleDevice> devices) {
        mDatas = checkNotNull(devices);
        mFragment = checkNotNull(fragment);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_name)
        TextView txtName;
        @BindView(R.id.txt_mac)
        TextView txtMac;
        @BindView(R.id.txt_rssi)
        TextView txtRssi;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    interface OnAdapterItemClickListener {
        void onAdapterViewClick(View view);
    }

    private OnAdapterItemClickListener onAdapterItemClickListener;

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final BleDevice device = mDatas.get(position);
        holder.txtName.setText(String.format(Locale.getDefault(), "%s", device.getName()));
        holder.txtName.setTextColor(mFragment.getResources().getColor(R.color.ble_device_name));
        holder.txtMac.setText(String.format(Locale.getDefault(), "%s", device.getAddress()));
        holder.txtMac.setTextColor(mFragment.getResources().getColor(R.color.ble_device_address));
        holder.txtRssi.setText(String.format(Locale.getDefault(), "%s", device.getRssi()));
        holder.txtRssi.setTextColor(mFragment.getResources().getColor(R.color.black));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_bledevice_item, parent, false);
        itemView.setOnClickListener(v -> {
            if (onAdapterItemClickListener != null) {
                onAdapterItemClickListener.onAdapterViewClick(v);
            }
        });
        return new ViewHolder(itemView);
    }

    void setOnAdapterItemClickListener(OnAdapterItemClickListener onAdapterItemClickListener) {
        this.onAdapterItemClickListener = onAdapterItemClickListener;
    }
}
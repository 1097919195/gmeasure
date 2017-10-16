package com.npclo.gdemo.main.measure;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.npclo.gdemo.R;
import com.npclo.gdemo.data.quality.Part;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Part> {
    private static final String TAG = ItemAdapter.class.getSimpleName();
    private Context mContext;
    private int layoutResourceId;
    private ArrayList<Part> mGridData = new ArrayList<>();

    public ItemAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Part> objects) {
        super(context, resource);
        this.mContext = context;
        this.layoutResourceId = resource;
        this.mGridData = objects;
    }

    public void setGridData(ArrayList<Part> mGridData) {
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Part item = mGridData.get(position);

        TextView textView = holder.name;
        textView.setText(item.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return mGridData.size();
    }

    @Override
    public Part getItem(int position) {
        return mGridData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView name;
        TextView diff;
    }
}
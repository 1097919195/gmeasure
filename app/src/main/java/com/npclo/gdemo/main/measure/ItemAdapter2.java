package com.npclo.gdemo.main.measure;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.npclo.gdemo.R;
import com.npclo.gdemo.utils.views.MyTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Endless on 2017/10/17.
 */

class ItemAdapter2 extends ArrayAdapter<String> {
    private Context mContext;
    private int layoutResourceId;
    private List<String> mGridData = new ArrayList<>();

    public ItemAdapter2(@NonNull Context context, @LayoutRes int resource, List<String> objects) {
        super(context, resource);
        this.mContext = context;
        this.layoutResourceId = resource;
        this.mGridData = objects;
    }

    public void setGridData(ArrayList<String> mGridData) {
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
            holder.name = (MyTextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String s = mGridData.get(position);

        MyTextView textView = holder.name;
        textView.setText(s);
        return convertView;
    }

    @Override
    public int getCount() {
        return mGridData.size();
    }

    @Override
    public String getItem(int position) {
        return mGridData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        MyTextView name;
    }
}

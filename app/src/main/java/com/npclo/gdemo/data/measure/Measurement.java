package com.npclo.gdemo.data.measure;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.npclo.gdemo.data.measure.item.MeasurementItem;
import com.npclo.gdemo.data.wuser.WechatUser;

public class Measurement {
    public Measurement(@Nullable WechatUser wechatUser, @Nullable MeasurementItem data, @NonNull String userid,
                       @NonNull String id) {
        _id = id;
        mData = data;
        uid = userid;
        user = wechatUser;
    }

    public Measurement(@Nullable WechatUser user, @Nullable MeasurementItem data, @NonNull String userid) {
        this(user, data, userid, "");
    }

    @Nullable
    private String _id;
    private MeasurementItem mData;
    private WechatUser user;
    private String uid;

    @Nullable
    public String get_id() {
        return _id;
    }

    public void set_id(@Nullable String _id) {
        this._id = _id;
    }

    public MeasurementItem getmData() {
        return mData;
    }

    public void setmData(MeasurementItem data) {
        this.mData = data;
    }

    public WechatUser getUser() {
        return user;
    }

    public void setUser(WechatUser user) {
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
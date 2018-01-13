package com.npclo.imeasurer.data.measure;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.npclo.imeasurer.data.WechatUser;

import java.util.List;

/**
 * @author Endless
 */
public class Measurement {
    public Measurement(@NonNull WechatUser wechatUser, @NonNull List<Part> data) {
        this.data = data;
        user = wechatUser;
    }


    @Nullable
    private String _id;
    private List<Part> data;
    private WechatUser user;
    private String uid;
    private String orgId;

    public WechatUser getUser() {
        return user;
    }

    public void setUser(WechatUser user) {
        this.user = user;
    }
}
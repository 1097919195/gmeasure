package com.npclo.imeasurer.data.measure;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.npclo.imeasurer.data.WechatUser;

import java.util.List;

/**
 * @author Endless
 */
public class Measurement {
    private Measurement(@NonNull WechatUser wechatUser, @NonNull List<Part> data, @NonNull String userid,
                        @Nullable String id, @Nullable String oid) {
        _id = id;
        this.data = data;
        uid = userid;
        user = wechatUser;
        orgId = oid;
    }

    public Measurement(@NonNull WechatUser user, @NonNull List<Part> data, @NonNull String userid, @Nullable String oid) {
        this(user, data, userid, "", oid);
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
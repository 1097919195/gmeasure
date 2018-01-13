package com.npclo.imeasurer.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 *
 * @author Endless
 * @date 2017/8/14
 */

public class WechatUser implements Parcelable {
    private int gender;
    private String nickname;
    @Nullable
    private String height;
    @Nullable
    private String weight;
    @Nullable
    private String openID;
    @Nullable
    private String avatar;
    @NonNull
    private String name;
    @Nullable
    private int times;

    protected WechatUser(Parcel in) {
        gender = in.readInt();
        nickname = in.readString();
        height = in.readString();
        weight = in.readString();
        openID = in.readString();
        avatar = in.readString();
        name = in.readString();
        times = in.readInt();
    }

    public static final Creator<WechatUser> CREATOR = new Creator<WechatUser>() {
        @Override
        public WechatUser createFromParcel(Parcel in) {
            return new WechatUser(in);
        }

        @Override
        public WechatUser[] newArray(int size) {
            return new WechatUser[size];
        }
    };

    @Nullable
    public int getTimes() {
        return times;
    }

    public void setTimes(@Nullable int times) {
        this.times = times;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Nullable String avatar) {
        this.avatar = avatar;
    }

    public int getGender() {
        return gender;
    }

    public WechatUser setGender(int gender) {
        this.gender = gender;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public WechatUser setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Nullable
    public String getHeight() {
        return height;
    }

    public WechatUser setHeight(@Nullable String height) {
        this.height = height;
        return this;
    }

    @Nullable
    public String getWeight() {
        return weight;
    }

    public WechatUser setWeight(@Nullable String weight) {
        this.weight = weight;
        return this;
    }

    @Nullable
    public String getOpenID() {
        return openID;
    }

    public WechatUser setOpenID(@Nullable String openID) {
        this.openID = openID;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(gender);
        parcel.writeString(nickname);
        parcel.writeString(height);
        parcel.writeString(weight);
        parcel.writeString(openID);
        parcel.writeString(avatar);
        parcel.writeString(name);
        parcel.writeInt(times);
    }
}

package com.npclo.imeasurer.data.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * @author Endless
 *         量体师类
 * @date 2017/7/19
 */

public class User implements Parcelable {
    protected User(Parcel in) {
        name = in.readString();
        pwd = in.readString();
        _id = in.readString();
        currTimes = in.readInt();
        totalTimes = in.readInt();
        code = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    @Nullable
    public String get_id() {
        return _id;
    }

    public void set_id(@Nullable String _id) {
        this._id = _id;
    }

    private String name;
    @Nullable
    private String pwd;
    @Nullable
    private String _id;
    @Nullable
    private int currTimes;
    @Nullable
    private int totalTimes;
    private int code;
    private String orgId;
    private String nickname;
    @Nullable
    private String logo;
    @Nullable
    private String title;

    @Nullable
    public String getPwd() {
        return pwd;
    }

    public void setPwd(@Nullable String pwd) {
        this.pwd = pwd;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getLogo() {
        return logo;
    }

    public void setLogo(@Nullable String logo) {
        this.logo = logo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Nullable
    public int getCurrTimes() {
        return currTimes;
    }

    public void setCurrTimes(@Nullable int currTimes) {
        this.currTimes = currTimes;
    }

    @Nullable
    public int getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(@Nullable int totalTimes) {
        this.totalTimes = totalTimes;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(pwd);
        dest.writeString(_id);
        dest.writeInt(currTimes);
        dest.writeInt(totalTimes);
        dest.writeInt(code);
    }
}

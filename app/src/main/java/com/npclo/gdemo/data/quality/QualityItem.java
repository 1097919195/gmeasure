package com.npclo.gdemo.data.quality;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author Endless
 * @date 2017/10/14
 */

public class QualityItem implements Parcelable {
    @Nullable
    private String _id;
    @Nullable
    private String code;
    @Nullable
    private String category;
    @Nullable
    private List<Part> parts;

    public QualityItem(String id, List<Part> p) {
        _id = id;
        parts = p;
    }

    protected QualityItem(Parcel in) {
        _id = in.readString();
        category = in.readString();
        code = in.readString();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static final Creator<QualityItem> CREATOR = new Creator<QualityItem>() {
        @Override
        public QualityItem createFromParcel(Parcel in) {
            return new QualityItem(in);
        }

        @Override
        public QualityItem[] newArray(int size) {
            return new QualityItem[size];
        }
    };

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(category);
        dest.writeString(code);
    }
}

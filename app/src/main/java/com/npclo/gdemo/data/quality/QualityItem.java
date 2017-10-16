package com.npclo.gdemo.data.quality;

import java.util.List;

/**
 * Created by Endless on 2017/10/14.
 */

public class QualityItem {
    private String _id;
    private String category;
    private List<Part> partList;

    public List<Part> getPartList() {
        return partList;
    }

    public void setPartList(List<Part> partList) {
        this.partList = partList;
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
}

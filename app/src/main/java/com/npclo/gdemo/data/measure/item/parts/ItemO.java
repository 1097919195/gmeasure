package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemO extends Part {

    public ItemO() {
    }

    public ItemO(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "上凸肚角度";
    }

    @Override
    public String getEn() {
        return "ItemO";
    }
}

package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemN extends Part {

    public ItemN() {
    }

    public ItemN(String cn, String en) {
        super(cn, en);
    }

    @Override
    public String getCn() {
        return "凹腰角度";
    }

    @Override
    public String getEn() {
        return "ItemN";
    }
}

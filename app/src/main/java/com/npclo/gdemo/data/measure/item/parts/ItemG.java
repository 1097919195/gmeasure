package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemG extends Part {

    public ItemG() {
    }

    public ItemG(String cn, String en) {
        super(cn, en);
    }

    @Override
    public String getCn() {
        return "臂围";
    }

    @Override
    public String getEn() {
        return "ItemG";
    }
}

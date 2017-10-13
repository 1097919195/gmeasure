package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemU extends Part {

    public ItemU() {
    }

    public ItemU(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "小腿围";
    }

    @Override
    public String getEn() {
        return "ItemU";
    }
}

package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemB extends Part {
    public ItemB() {
    }

    public ItemB(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "颈围";
    }

    @Override
    public String getEn() {
        return "ItemB";
    }
}

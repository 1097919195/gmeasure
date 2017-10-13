package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemR extends Part {

    public ItemR() {
    }

    public ItemR(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "大腿围";
    }

    @Override
    public String getEn() {
        return "ItemR";
    }
}

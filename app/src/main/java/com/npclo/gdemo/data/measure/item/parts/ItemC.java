package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemC extends Part {
    public ItemC() {
    }

    public ItemC(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "肩宽";
    }

    @Override
    public String getEn() {
        return "ItemC";
    }
}

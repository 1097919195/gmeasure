package com.npclo.imeasurer.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemA extends Part {
    public ItemA() {
    }

    public ItemA(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "头围";
    }

    @Override
    public String getEn() {
        return "ItemA";
    }
}

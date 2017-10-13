package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemF extends Part {
    public ItemF() {
    }

    public ItemF(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "弯背角度";
    }

    @Override
    public String getEn() {
        return "ItemF";
    }
}

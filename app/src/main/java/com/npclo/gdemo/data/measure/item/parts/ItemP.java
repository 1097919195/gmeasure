package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemP extends Part {

    public ItemP() {
    }

    public ItemP(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "下凸肚角度";
    }

    @Override
    public String getEn() {
        return "ItemP";
    }
}

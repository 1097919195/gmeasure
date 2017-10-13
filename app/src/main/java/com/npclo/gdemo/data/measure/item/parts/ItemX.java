package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemX extends Part {

    public ItemX(String cn, String en) {
        super(cn, en);
    }

    public ItemX() {
    }

    public String getCn() {
        return "髂骨高度";
    }

    @Override
    public String getEn() {
        return "ItemX";
    }
}

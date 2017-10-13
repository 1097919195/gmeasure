package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemE extends Part {

    public ItemE() {
    }

    public ItemE(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "右肩斜";
    }

    @Override
    public String getEn() {
        return "ItemE";
    }
}

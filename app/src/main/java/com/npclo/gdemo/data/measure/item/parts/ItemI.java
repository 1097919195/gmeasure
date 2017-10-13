package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemI extends Part {

    public ItemI() {
    }

    public ItemI(String cn, String en) {
        super(cn, en);
    }

    @Override
    public String getCn() {
        return "臂长";
    }

    @Override
    public String getEn() {
        return "ItemI";
    }
}

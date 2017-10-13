package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemJ extends Part {

    public ItemJ() {
    }

    public ItemJ(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "胸围";
    }

    @Override
    public String getEn() {
        return "ItemJ";
    }
}

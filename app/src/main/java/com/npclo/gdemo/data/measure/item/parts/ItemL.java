package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemL extends Part {

    public ItemL() {
    }

    public ItemL(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "肚围";
    }

    @Override
    public String getEn() {
        return "ItemL";
    }
}

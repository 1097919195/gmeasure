package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemW extends Part {

    public ItemW() {
    }

    public ItemW(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "通裆";
    }

    @Override
    public String getEn() {
        return "ItemW";
    }
}

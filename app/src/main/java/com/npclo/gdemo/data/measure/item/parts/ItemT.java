package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemT extends Part {

    public ItemT() {
    }

    public ItemT(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "膝围";
    }

    @Override
    public String getEn() {
        return "ItemT";
    }
}

package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemQ extends Part {

    public ItemQ() {
    }

    public ItemQ(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "臀围";
    }

    @Override
    public String getEn() {
        return "ItemQ";
    }
}

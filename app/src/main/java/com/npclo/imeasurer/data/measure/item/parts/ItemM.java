package com.npclo.imeasurer.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemM extends Part {


    public ItemM() {
    }

    public ItemM(String cn, String en) {
        super(cn, en);
    }

    public String getCn() {
        return "腰围";
    }

    @Override
    public String getEn() {
        return "ItemM";
    }
}

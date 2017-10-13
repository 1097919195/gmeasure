package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemD extends Part {

    public ItemD() {
    }

    public ItemD(String cn, String en) {
        super(cn, en);
    }

    @Override
    public String getCn() {
        return "左肩斜";
    }

    @Override
    public String getEn() {
        return "ItemD";
    }
}
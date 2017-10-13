package com.npclo.gdemo.data.measure.item.parts;

/**
 * Created by Endless on 2017/8/5.
 */

public class ItemK extends Part {
    public ItemK() {
    }

    public ItemK(String cn, String en) {
        super(cn, en);
    }
//    private String cn = "下胸围";
//    private String en = "ItemK";

    public String getCn() {
        return "下胸围";
    }

    @Override
    public String getEn() {
        return "ItemK";
    }
}

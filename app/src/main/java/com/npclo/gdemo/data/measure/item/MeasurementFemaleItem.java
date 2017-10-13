package com.npclo.gdemo.data.measure.item;

import com.npclo.gdemo.data.measure.item.parts.ItemK;

public class MeasurementFemaleItem extends MeasurementItem {
    private com.npclo.gdemo.data.measure.item.parts.ItemK ItemK;

    public ItemK getItemK() {
        return ItemK;
    }

    public MeasurementFemaleItem setItemK(ItemK itemK) {
        ItemK = itemK;
        return this;
    }
}
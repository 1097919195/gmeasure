package com.npclo.gdemo.data.quality;

import android.support.annotation.Nullable;

/**
 * @author Endless
 * @date 2017/10/14
 */

public class Part {
    /**
     * 质检部位名称
     */
    private String name;
    /**
     * 原始值，标准尺寸
     */
    private float oriValue;
    /**
     * 真实值，实际测量后的尺寸
     */
    @Nullable
    private float actValue;
    /**
     * 是否合格。合格：0，不合格：-1
     */
    @Nullable
    private int code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getOriValue() {
        return oriValue;
    }

    public void setOriValue(float oriValue) {
        this.oriValue = oriValue;
    }

    public float getActValue() {
        return actValue;
    }

    public void setActValue(float actValue) {
        this.actValue = actValue;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

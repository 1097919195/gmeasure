package com.npclo.imeasurer.data.measure;

/**
 * @author Endless
 * @date 2017/8/5
 */

public class Part {
    private String cn;
    private float value;
    private boolean isAngle = false;

    public boolean isAngle() {
        return isAngle;
    }

    public void setAngle(boolean angle) {
        isAngle = angle;
    }

    public Part(String cn) {
        this.cn = cn;
    }

    public Part() {
    }

    public Part(String cn, float value) {
        this.cn = cn;
        this.value = value;
    }

    public Part(String cn, boolean isAngle) {
        this.cn = cn;
        this.isAngle = isAngle;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}

package com.npclo.imeasurer.data.measure;

import android.util.Log;

/**
 * Created by Administrator on 2018/3/21 0021.
 */

public class MessageEvent {
    private int num = 1;
    public MessageEvent(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}

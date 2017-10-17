package com.npclo.gdemo.utils.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.npclo.gdemo.utils.MeasureStateEnum;

/**
 * Created by Endless on 2017/10/16.
 */

public class MyLineLayout extends LinearLayout {
    public MyLineLayout(Context context) {
        super(context);
    }

    public MyLineLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyLineLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }


    private int state = MeasureStateEnum.UNMEASUED.ordinal();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}

package com.npclo.imeasurer.utils.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.npclo.imeasurer.R;
import com.npclo.imeasurer.utils.MeasureStateEnum;

public class MyTextView extends AppCompatTextView {
    private float value;
    private int state = MeasureStateEnum.UNMEASUED.ordinal();

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyTextView);
        value = array.getFloat(R.styleable.MyTextView_value, 0);
        array.recycle();
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
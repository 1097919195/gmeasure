package com.npclo.imeasurer.utils;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseApplication;

/**
 * Created by Endless on 2017/12/1.
 */

public class SettingUtil {
    private SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getAppContext);

    public static SettingUtil getInstance() {
        return SettingsUtilInstance.instance;
    }

    /**
     * 获取滑动返回值
     */
    public int getSlidable() {
        String s = setting.getString("slidable", "1");
        return Integer.parseInt(s);
    }

    public int getCustomIconValue() {
        String s = setting.getString("custom_icon", "0");
        return Integer.parseInt(s);
    }

    public int getColor() {
        int defaultColor = BaseApplication.getAppContext.getResources().getColor(R.color.primary);
        int color = setting.getInt("color", defaultColor);
        if ((color != 0) && Color.alpha(color) != 255) {
            return defaultColor;
        }
        return color;
    }

    public boolean getNavBar() {
        return setting.getBoolean("nav_bar", false);
    }


    private static final class SettingsUtilInstance {
        private static final SettingUtil instance = new SettingUtil();
    }
}

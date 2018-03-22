package com.npclo.imeasurer.utils;

import com.npclo.imeasurer.BuildConfig;
import com.npclo.imeasurer.R;

/**
 * @author Endless
 * @date 2017/12/1
 */

public class Constant {
    public static final int[] ICONS_DRAWABLES = new int[]{
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher
    };

    public static final String PRODUCTION_API_URL = "https://www.npclo.com/api/";
    public static final String DEVELOPMENT_API_URL = "http://www.npclo.com/api/";
    public static final String MANUAL = "manual";
    public static final String AUTO = "auto";
    public static final int USER_PWD = 1;
    public static final int USER_FEEDBACK = 2;
    public static final int USER_CONTACT = 3;
    public static final int USER_INSTRUCTION = 4;
    public static final String APP_KEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
    public static final String APP_SECRET = "29aa998c451d64d9334269546a4021b8";
    public static final String PRODUCTION_IMG_URL = "https://img.npclo.com/org_logo/";
    public static final String DEVELOPMENT_IMG_URL = "http://img.npclo.com/org_logo/";

    public static String getApiUrl() {
        if ("production".equals(BuildConfig.ENV)) {
            return PRODUCTION_API_URL;
        } else if ("development".equals(BuildConfig.ENV)) {
            return DEVELOPMENT_API_URL;
        }
        return DEVELOPMENT_API_URL;
    }

    public static String getImgUrl() {
        if ("production".equals(BuildConfig.ENV)) {
            return PRODUCTION_IMG_URL;
        } else if ("development".equals(BuildConfig.ENV)) {
            return DEVELOPMENT_IMG_URL;
        }
        return DEVELOPMENT_IMG_URL;
    }
}

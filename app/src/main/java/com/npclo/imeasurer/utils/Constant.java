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

    public static final int USER_PWD = 1;
    public static final int USER_FEEDBACK = 2;
    public static final int USER_CONTACT = 3;
    public static final int USER_INSTRUCTION = 4;
    public static final String APP_KEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
    public static final String APP_SECRET = "29aa998c451d64d9334269546a4021b8";

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    public static final String API_BASE_URL = "://www.npclo.com/api/";

    public static final String MANUAL = "manual";
    public static final String AUTO = "auto";

    public static String getHttpScheme() {
        if ("production".equals(BuildConfig.ENV)) {
            return Constant.SCHEME_HTTPS;
        } else if ("development".equals(BuildConfig.ENV)) {
            return Constant.SCHEME_HTTP;
        }
        return SCHEME_HTTP;
    }
}

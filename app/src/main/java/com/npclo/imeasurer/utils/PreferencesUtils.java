/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.npclo.imeasurer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Endless
 */
public final class PreferencesUtils {

    private static final String CURRENT_DATE = "currentDate";
    private static final String TOKEN = "token";
    private static final String LOGIN_NAME = "loginName";
    private static final String LOGIN_PWD = "loginPwd";

    private static final String MAC_ADDRESS = "macAddress";
    private static final String DEVICE_NAME = "deviceName";
    private static final String DEVICE_UUID = "deviceUUID";

    private static final String USER_ID = "userId";
    private static final String CONTRACT_NAME = "contractName";
    private static final String CURR_TIMES = "currTimes";
    private static final String TOTAL_TIMES = "totalTimes";
    private static final String USER_NAME = "userName";
    private static final String USER_NICKNAME = "userNickname";
    private static final String USER_LOGO = "userLogo";
    private static final String USER_TITLE = "userTitle";
    private static final String USER_ORGID = "userOrgId";

    private static final String MEASURE_ITEMS = "measureItems";
    private static final String MEASURE_CID = "measureCid";
    private static final String MEASURE_NUM = "measureNum";
    private static final String MEASURE_MEASURED = "measureMeasured";
    private static final String MEASURE_OFFSET = "measureOffset";

    private static final String FIRST_START = "firstStart";

    private static PreferencesUtils sInstance;

    private static SharedPreferences mPreferences;

    private PreferencesUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesUtils getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public boolean isCurrentDayFirst(String str) {
        return !mPreferences.getString(CURRENT_DATE, "").equals(str);
    }

    public void setCurrentDate(String str) {
        assign(CURRENT_DATE, str);
    }

    public String getToken(boolean checkLogin) {
        if (checkLogin) {
            return mPreferences.getString(TOKEN, "");
        } else {
            return mPreferences.getString(TOKEN, "thisisthedefaultjwt");
        }
    }

    public void setToken(String token) {
        assign(TOKEN, token);
    }

    private void assign(String name, String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(name, value);
        editor.apply();
    }

    private void assign(String name, int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    private void assign(String name, float value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(name, value);
        editor.apply();
    }

    private void assign(String name, boolean blo) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(name, blo);
        editor.apply();
    }

    public String getLoginName() {
        return mPreferences.getString(LOGIN_NAME, "");
    }

    public void setLoginName(String loginName) {
        assign(LOGIN_NAME, loginName);
    }

    public String getLoginPwd() {
        return mPreferences.getString(LOGIN_PWD, "");
    }

    public void setLoginPwd(String loginPwd) {
        assign(LOGIN_PWD, loginPwd);
    }

    public String getMacAddress() {
        return mPreferences.getString(MAC_ADDRESS, "");
    }

    public void setMacAddress(String macAddress) {
        assign(MAC_ADDRESS, macAddress);
    }

    public String getDeviceName() {
        return mPreferences.getString(DEVICE_NAME, "");
    }

    public void setDeviceName(String deviceName) {
        assign(DEVICE_NAME, deviceName);
    }

    public String getDeviceUuid() {
        return mPreferences.getString(DEVICE_UUID, "");
    }

    public void setDeviceUuid(String deviceUuid) {
        assign(DEVICE_UUID, deviceUuid);
    }

    public String getUserId() {
        return mPreferences.getString(USER_ID, "");
    }

    public void setUserId(String userId) {
        assign(USER_ID, userId);
    }

    public String getContractName() {
        return mPreferences.getString(CONTRACT_NAME, "");
    }

    public void setContractName(String contractName) {
        assign(CONTRACT_NAME, contractName);
    }

    public int getCurrTimes() {
        return mPreferences.getInt(CURR_TIMES, 0);
    }

    public void setCurrTimes(int currTimes) {
        assign(CURR_TIMES, currTimes);
    }

    public int getTotalTimes() {
        return mPreferences.getInt(TOTAL_TIMES, 0);
    }

    public void setTotalTimes(int totalTimes) {
        assign(TOTAL_TIMES, totalTimes);
    }

    public String getUserName() {
        return mPreferences.getString(USER_NAME, "");
    }

    public void setUserName(String userName) {
        assign(USER_NAME, userName);
    }

    public String getUserNickname() {
        return mPreferences.getString(USER_NICKNAME, "");
    }

    public void setUserNickname(String userNickname) {
        assign(USER_NICKNAME, userNickname);
    }

    public String getUserLogo() {
        return mPreferences.getString(USER_LOGO, "");
    }

    public void setUserLogo(String userLogo) {
        assign(USER_LOGO, userLogo);
    }

    public String getUserTitle() {
        return mPreferences.getString(USER_TITLE, "");
    }

    public void setUserTitle(String userTitle) {
        assign(USER_TITLE, userTitle);
    }

    public String getUserOrgid() {
        return mPreferences.getString(USER_ORGID, "");
    }

    public void setUserOrgid(String userOrgid) {
        assign(USER_ORGID, userOrgid);
    }

    public String getMeasureItems() {
        return mPreferences.getString(MEASURE_ITEMS, "");
    }

    public void setMeasureItems(String measureItems) {
        assign(MEASURE_ITEMS, measureItems);
    }

    //自由量体，合同id固定为10000
    public String getMeasureCid() {
        return mPreferences.getString(MEASURE_CID, "10000");
    }

    public void setMeasureCid(String measureCid) {
        assign(MEASURE_CID, measureCid);
    }

    public int getMeasureNum() {
        return mPreferences.getInt(MEASURE_NUM, 0);
    }

    public void setMeasureNum(int measureNum) {
        assign(MEASURE_NUM, measureNum);
    }

    public int getMeasureMeasured() {
        return mPreferences.getInt(MEASURE_MEASURED, 0);
    }

    public void setMeasureMeasured(int measureMeasured) {
        assign(MEASURE_MEASURED, measureMeasured);
    }

    public float getMeasureOffset() {
        return mPreferences.getFloat(MEASURE_OFFSET, 14.0f);
    }

    public void setMeasureOffset(float measureOffset) {
        assign(MEASURE_OFFSET, measureOffset);
    }

    public boolean getFirstStart() {
        return mPreferences.getBoolean(FIRST_START, false);
    }

    public void setFirstStart(boolean first) {
        assign(FIRST_START, first);
    }
}
package com.npclo.gdemo.utils.http;


import com.npclo.gdemo.data.quality.QualityItem;

import rx.Observable;

/**
 * @author Endless
 */
public class DemoHelper extends HttpHelper {
    /**
     * 根据二维码扫码结果id获取信息
     *
     * @param id 二维码扫码结果
     * @return
     */
    public Observable<QualityItem> getQualityItemWithId(String id) {
        return retrofit.create(DemoService.class)
                .getQualityItemWithId(id)
                .map(new HttpResponseFunc<>());
    }

    /**
     * 手动输入二维码编号获取信息
     * @param code 二维码编号
     * @return
     */
    public Observable<QualityItem> getQualityItemWithCode(String code) {
        return retrofit.create(DemoService.class)
                .getQualityItemWithCode(code)
                .map(new HttpResponseFunc<>());
    }
}

package com.npclo.gdemo.main.quality;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;

/**
 * Created by Endless on 2017/10/13.
 */

public interface QualityContract {
    interface Presenter extends BasePresenter {
    }

    interface View extends BaseView<Presenter> {
    }
}

package com.npclo.imeasurer.data.user;

import com.npclo.imeasurer.data.HttpMsg;
import com.npclo.imeasurer.data.ValidCode;
import com.npclo.imeasurer.data.wuser.WechatUser;
import com.npclo.imeasurer.utils.http.user.UserHttpHelper;

import rx.Observable;

/**
 * Created by Endless on 2017/7/20.
 */

public class UserRepository implements UserDataSource {
    @Override
    public Observable<User> signIn(String name, String pwd) {
        return new UserHttpHelper().signIn(name, pwd);
    }

    @Override
    public Observable<User> signUp(String name, String pwd, String code) {
        return new UserHttpHelper().signUp(name, pwd, code);
    }

    @Override
    public Observable<ValidCode> getValidCode(String name, String type) {
        return new UserHttpHelper().getValidCode(name, type);
    }

    public Observable<HttpMsg> resetPwd(String mobile, String pwd, String code) {
        return new UserHttpHelper().resetPwd(mobile, pwd, code);
    }

    public Observable<WechatUser> getUserInfoWithCode(String code) {
        return new UserHttpHelper().getUserInfoWithCode(code);
    }

    public Observable<HttpMsg> editPwd(String id, String old, String newpwd) {
        return new UserHttpHelper().editPwd(id, old, newpwd);
    }

    public Observable<WechatUser> getUserInfoWithOpenID(String id) {
        return new UserHttpHelper().getUserInfoWithOpenID(id);
    }

}
package com.tony.newsmth.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tony.newsmth.R;
import com.tony.newsmth.utils.Settings;
import com.tony.newsmth.net.AjaxResponse;
import com.tony.newsmth.net.SmthApi;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by l00151177 on 2016/9/22.
 */
public class LoginControllerImpl implements ILoginController{
    private static final String TAG = "LoginControllerImpl";
    private Context mContext = null;
    public LoginControllerImpl(Context context) {
        mContext = context;
    }
    @Override
    public boolean onLogin(final String userName, final String password, String cookieDate) {
        SmthApi netApi = SmthApi.getInstance(mContext);
        netApi.getCookieJar().clear();
        netApi.getService().login(userName, password, "2")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "login(). onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (onLoginResultListener != null) {
                            onLoginResultListener.onResult(LoginResult.FAILED_COMMON, "connection failed.");
                        }
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
//                        {"ajax_st":0,"ajax_code":"0101","ajax_msg":"您的用户名并不存在，或者您的密码错误"}
//                        {"ajax_st":0,"ajax_code":"0105","ajax_msg":"请勿频繁登录"}
//                        {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
                        switch (ajaxResponse.getAjax_st()) {
                            case AjaxResponse.AJAX_RESULT_OK:
                                Toast.makeText(mContext, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
                                // save username & passworld
                                Settings.getInstance(mContext).setUsername(userName);
                                Settings.getInstance(mContext).setPassword(password);
                                if (onLoginResultListener != null) {
                                    onLoginResultListener.onResult(LoginResult.SUCCESS, ajaxResponse.toString());
                                }
                                break;
                            default:
                                Toast.makeText(mContext, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
                                if (onLoginResultListener != null) {
                                    LoginResult resultCode;
                                    switch (ajaxResponse.getAjax_code()) {
                                        case "0101":
                                            resultCode = LoginResult.FAILED_INVALIDE_PARAM;
                                            break;
                                        case "0105":
                                            resultCode = LoginResult.FAILED_TOO_MUCH_ACTION;
                                            break;
                                        case "0005":
                                            resultCode = LoginResult.SUCCESS;
                                            break;
                                        default:
                                            resultCode = LoginResult.FAILED_COMMON;
                                            break;
                                    }
                                    onLoginResultListener.onResult(resultCode, ajaxResponse.toString());
                                }
                                break;
                        }
                    }
                });
        return true;
    }

    @Override
    public boolean onGuestLogin() {
        return false;
    }

    @Override
    public boolean onLogout() {
        SmthApi netApi = SmthApi.getInstance(mContext);
        netApi.getService().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (onLogoutResultListener != null) {
                            onLogoutResultListener.onResult(LoginResult.FAILED_COMMON, mContext.getString(R.string.logout_failed));
                        }
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
                        if (onLogoutResultListener != null) {
                            onLogoutResultListener.onResult(LoginResult.SUCCESS, ajaxResponse.getAjax_msg());
                        }
                    }
                });
        return true;
    }

    private OnLoginResultListener onLoginResultListener = null;
    public void setOnLoginResultListener(OnLoginResultListener l) {
        onLoginResultListener = l;
    }

    private OnLogoutResultListener onLogoutResultListener = null;
    public void setOnLogoutResultListener(OnLogoutResultListener l) {
        onLogoutResultListener = l;
    }
}

package com.tony.newsmth.controller;

/**
 * Created by l00151177 on 2016/9/22.
 */
public interface ILoginController {
    boolean onLogin(String userName, String passWord, String cookieDate);
    boolean onGuestLogin();
    boolean onLogout();
    enum LoginResult {
        SUCCESS, FAILED_COMMON, FAILED_TIMEOUT, FAILED_INVALIDE_PARAM, FAILED_TOO_MUCH_ACTION
    }
    void setOnLoginResultListener(OnLoginResultListener l);
    interface OnLoginResultListener {
        boolean onResult(LoginResult result, String prompt);
    }
    void setOnLogoutResultListener(OnLogoutResultListener l);
    interface OnLogoutResultListener {
        boolean onResult(LoginResult result, String prompt);
    }
}

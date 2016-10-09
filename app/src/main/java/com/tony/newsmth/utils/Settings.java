package com.tony.newsmth.utils;

/**
 * Created by l00151177 on 2016/9/21.
 * Usage:
 * String username = Settings.getInstance().getUsername();
 * Settings.getInstance().setUsername("mozilla");
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 how to add a new setting:
 1. create private String setting_key
 2. create private local variable
 3. init the variable in initSetting()
 4. implement get and set methods to access the setting
 */
public class Settings {
    private Context mContext = null;
    private static Settings instance = null;

    private Settings(Context context) {
        mContext = context.getApplicationContext();
        init();
    }

    private SharedPreferences mPreference = null;
    private SharedPreferences.Editor mEditor = null;

    private void init() {
        // this
        mPreference = mContext.getSharedPreferences("smth", Activity.MODE_PRIVATE);

        mUsername = mPreference.getString(USERNAME_KEY, "");
        mPassword = mPreference.getString(PASSWORD_KEY, "");
        bAutoLogin = mPreference.getBoolean(AUTO_LOGIN, true);
    }

    public static synchronized Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

    private static final String USERNAME_KEY = "username";
    private String mUsername;

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        if (mUsername == null || !mUsername.equals(username)) {
            mUsername = username;
            mEditor = mPreference.edit();
            mEditor.putString(USERNAME_KEY, this.mUsername);
            mEditor.apply();
        }
    }

    private static final String PASSWORD_KEY = "password";
    private String mPassword;

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        if (this.mPassword == null || !this.mPassword.equals(mPassword)) {
            this.mPassword = mPassword;
            mEditor = mPreference.edit();
            mEditor.putString(PASSWORD_KEY, this.mPassword);
            mEditor.apply();
        }
    }

    private static final String AUTO_LOGIN = "auto_login";
    private boolean bAutoLogin;

    public boolean isAutoLogin() {
        return bAutoLogin;
    }

    public void setAutoLogin(boolean mAutoLogin) {
        if (this.bAutoLogin != mAutoLogin) {
            this.bAutoLogin = mAutoLogin;
            mEditor = mPreference.edit();
            mEditor.putBoolean(AUTO_LOGIN, this.bAutoLogin);
            mEditor.apply();
        }
    }

    private static final String LAST_LOGIN_SUCCESS = "last_login_success";
    private boolean bLastLoginSuccess;

    public boolean isLastLoginSuccess() {
        return bLastLoginSuccess;
    }

    public void setLastLoginSuccess(boolean bLastLoginSuccess) {
        if (this.bLastLoginSuccess != bLastLoginSuccess) {
            this.bLastLoginSuccess = bLastLoginSuccess;
            mEditor.putBoolean(LAST_LOGIN_SUCCESS, bLastLoginSuccess);
            mEditor.commit();
        }
    }
}

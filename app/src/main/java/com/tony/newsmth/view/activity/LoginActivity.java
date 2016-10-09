package com.tony.newsmth.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tony.newsmth.R;
import com.tony.newsmth.controller.HotTopicControllerImpl;
import com.tony.newsmth.controller.IHotTopicController;
import com.tony.newsmth.model.Topic;
import com.tony.newsmth.utils.Settings;
import com.tony.newsmth.controller.ILoginController;
import com.tony.newsmth.controller.LoginControllerImpl;
import com.tony.newsmth.view.activity.BaseActivity;

import java.util.List;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    public static final String ACTION_HOT_TOPIC = "com.tony.newsmth.SHOW_HOT_TOPIC_LIST";
    EditText etUserName = null;
    EditText etPassword = null;
    Button btnLogin = null;
    Button btnGuestLogin = null;
    private ILoginController loginController = null;
    private IHotTopicController hotTopicController = null;
    boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initLoginController();
        initHotTopicController();
        String savedUserName = Settings.getInstance(this).getUsername();
        etUserName = (EditText) findViewById(R.id.user_name);
        if (savedUserName != null && TextUtils.isEmpty(savedUserName)) {
            etUserName.setText(savedUserName);
        }

        // TODO: unsafe way to save password.
        String savedPassword = Settings.getInstance(this).getUsername();
        etPassword = (EditText) findViewById(R.id.password);
        if (savedPassword != null && TextUtils.isEmpty(savedPassword)) {
            etPassword.setText(savedPassword);
        }

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLogin) {
                    handleLoginEvent();
                } else {
                    handleLogOutEvent();
                }
            }
        });
        btnGuestLogin = (Button) findViewById(R.id.btnGuestLogin);
        btnGuestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGuestLoginEvent();
            }
        });
    }

    private void initHotTopicController() {
        hotTopicController = new HotTopicControllerImpl(this);
        hotTopicController.setOnHotTopicResultListener(new IHotTopicController.OnHotTopicResultListener() {
            @Override
            public boolean onStart() {
                Log.d(TAG, "hotTopicController onStart");
                return true;
            }

            @Override
            public boolean onNewTopic(Topic topic) {
                Log.d(TAG, "hotTopicController onNewBoard");
                return true;
            }

            @Override
            public boolean onResult(IHotTopicController.HotTopicResult result, List<Topic> topicList, String prompt) {
                Log.d(TAG, "hotTopicController onResult");
                return true;
            }
        });
    }

    private void initLoginController() {
        loginController = new LoginControllerImpl(this);
        loginController.setOnLoginResultListener(new ILoginController.OnLoginResultListener() {
            @Override
            public boolean onResult(ILoginController.LoginResult result, String prompt) {
                switch (result) {
                    case SUCCESS:
                        isLogin = true;
                        btnLogin.setText(R.string.btn_logout);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });
        loginController.setOnLogoutResultListener(new ILoginController.OnLogoutResultListener() {
            @Override
            public boolean onResult(ILoginController.LoginResult result, String prompt) {
                switch (result) {
                    case SUCCESS:
                        isLogin = false;
                        btnLogin.setText(R.string.btn_login);
                        Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }
        });
    }

    private void handleLoginEvent() {
        Log.i(TAG, "handleLoginEvent");
        if (etUserName == null || etPassword == null ||
                etUserName.getText() == null || etPassword.getText() == null) {
            Log.e(TAG, "could not get user name or pass word");
            return;
        }
        final String userName = etUserName.getText().toString();
        final String password = etPassword.getText().toString();
        if (!checkUserName(userName)) {
            Log.e(TAG, "invalid user name");
            return;
        }
        if (!checkPassword(password)) {
            Log.e(TAG, "invalid password");
            return;
        }
        loginController.onLogin(userName,password,"2");
    }

    private boolean checkUserName(String userName) {
        return (userName != null && !TextUtils.isEmpty(userName));
    }

    private boolean checkPassword(String password) {
        return (password != null && !TextUtils.isEmpty(password));
    }

    private void handleLogOutEvent() {
        loginController.onLogout();
    }

    private void handleGuestLoginEvent() {
        Log.i(TAG, "handleGuestLoginEvent");
        //loginController.onGuestLogin();
        showHotTopicActivity();
    }

    private void showHotTopicActivity() {
        Intent intent = new Intent(ACTION_HOT_TOPIC);
        startActivity(intent);
        finish();
    }
}

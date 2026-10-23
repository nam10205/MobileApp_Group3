package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.myapplication.activities.LoginActivity;

// Entry point: opens the saved account's home screen, or the login screen if no one is logged in.
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = LoginActivity.getPrefs(this);
        if (sp.getBoolean("isLogin", false)) {
            LoginActivity.openHome(this, sp.getString("role", null));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}

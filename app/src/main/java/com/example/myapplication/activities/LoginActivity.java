package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.dal.DBHelper;
import com.example.myapplication.models.Account;

public class LoginActivity extends Activity {
    public static final String PREFS_NAME = "LOGIN";

    EditText edtUser, edtPass;
    Button btnLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);

        dbHelper = new DBHelper(this);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {

        String user = edtUser.getText().toString().trim();
        String pass = edtPass.getText().toString();
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Account account = dbHelper.getAccount(user);

        if (account != null && account.getPassword().equals(DBHelper.hashPassword(pass))) {

            SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putInt("accountId", account.getId());
            editor.putString("username", user);
            editor.putString("role", account.getRole());
            editor.putBoolean("isLogin", true);
            editor.apply();

            openHome(this, account.getRole());
            finish();

        } else {
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openHome(Context context, String role) {
        Class<?> target = Account.ROLE_ADMIN.equals(role) ? AdminActivity.class : CustomerActivity.class;
        context.startActivity(new Intent(context, target));
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    public static void logout(Activity activity) {
        getPrefs(activity).edit().clear().apply();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
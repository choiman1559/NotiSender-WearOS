package com.noti.sender.wear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.nongmsauth.FirebaseRestAuth;

public class SettingsActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SharedPreferences prefs = getSharedPreferences(getPackageName()  + "_preferences", Context.MODE_PRIVATE);

        TextView UID = findViewById(R.id.UID);
        Switch Enable = findViewById(R.id.Enable);
        LinearLayout Click = findViewById(R.id.ClickAble);

        UID.setText("UID : " + prefs.getString("UID","UNKNOWN"));
        Enable.setChecked(prefs.getBoolean("Enabled",false));
        Enable.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("Enabled",isChecked).apply());
        Click.setOnClickListener(v -> {
            FirebaseRestAuth.Companion.getInstance(FirebaseApp.getInstance()).signOut();
            GoogleSignIn.getClient(this,  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestId()
                    .build()).signOut();
            prefs.edit().remove("UID").remove("Enabled").apply();
            startActivity(new Intent(this,MainActivity.class));
            this.finish();
        });
    }
}
package com.noti.sender.wear;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.firebase.FirebaseApp;
import com.google.firebase.nongmsauth.FirebaseRestAuth;
import com.google.firebase.nongmsauth.FirebaseRestAuthUser;

import java.util.Objects;

public class MainActivity extends WearableActivity implements LifecycleOwner {

    private LifecycleRegistry mLifecycleRegistry;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseRestAuth auth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        UID_Init();

        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = FirebaseRestAuth.Companion.getInstance(FirebaseApp.getInstance());
        auth.getTokenRefresher().bindTo(this);

        TextView AuthButton = findViewById(R.id.AuthButton);
        AuthButton.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult account =  Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("dd",account.getSignInAccount().getIdToken());
            if(account.isSuccess()) {
                try {
                    firebaseAuthWithGoogle(account.getSignInAccount().getIdToken());
                } catch (Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed to Login!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to Login!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        final SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences",MODE_PRIVATE);
        auth.getTokenRefresher().onLifecycleStarted();
        auth.signInWithGoogle(idToken,"google.com")
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseRestAuthUser user = auth.getCurrentUser();
                        prefs.edit().putString("UID",user.getUserId()).apply();
                        Log.d("uid",user.getUserId());
                        UID_Init();
                    } else {
                        task.getException().printStackTrace();
                        Toast.makeText(MainActivity.this,"Failed to get user id",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void UID_Init() {
        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences",MODE_PRIVATE);
        if(!Objects.equals(prefs.getString("UID", ""), "")) {
            startActivity(new Intent(MainActivity.this,SettingActivity.class));
            this.finish();
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}

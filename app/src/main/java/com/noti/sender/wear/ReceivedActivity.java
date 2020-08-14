package com.noti.sender.wear;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReceivedActivity extends WearableActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_received);

        ImageView Close = findViewById(R.id.cancel);
        ImageView Ok = findViewById(R.id.ok);
        TextView Detail = findViewById(R.id.Details);

        Intent i = getIntent();
        String TOPIC = "/topics/" + getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE).getString("UID", "");
        String Package = i.getStringExtra("package");
        String APPNAME = i.getStringExtra("appname");
        String TITLE = i.getStringExtra("title");
        String DEVICE_NAME = i.getStringExtra("device_name");
        String DATE = i.getStringExtra("date");
        String detail = "";
        detail += "App Name : " + APPNAME + "\n";
        detail += "Noti Title : " + TITLE + "\n";
        detail += "Device : " + DEVICE_NAME + "\n";
        detail += "Posted Time : " + DATE + "\n";
        Detail.setText(detail);

        Close.setOnClickListener(v -> ReceivedActivity.this.finish());
        Ok.setOnClickListener(v -> {
            JSONObject notificationHead = new JSONObject();
            JSONObject notifcationBody = new JSONObject();
            try {
                notifcationBody.put("package", Package);
                notifcationBody.put("type", "reception");
                notifcationBody.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
                notifcationBody.put("send_device_name", DEVICE_NAME);
                notifcationBody.put("send_device_id", i.getStringExtra("device_id"));
                notificationHead.put("to", TOPIC);
                notificationHead.put("data", notifcationBody);
            } catch (JSONException e) {
                Log.e("Noti", "onCreate: " + e.getMessage());
            }
            sendNotification(notificationHead);
            ReceivedActivity.this.finish();
        });
    }

    private void sendNotification (JSONObject notification){
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + getString(R.string.serverKey);
        final String contentType = "application/json";
        final String TAG = "NOTIFICATION TAG";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> Toast.makeText(ReceivedActivity.this, "Failed to send Notification! Please check internet and try again!", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    static class MySingleton {
        private  static MySingleton instance;
        private RequestQueue requestQueue;
        private Context ctx;

        private MySingleton(Context context) {
            ctx = context;
            requestQueue = getRequestQueue();
        }

        public static synchronized MySingleton getInstance(Context context) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            }
            return requestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }
    }
}
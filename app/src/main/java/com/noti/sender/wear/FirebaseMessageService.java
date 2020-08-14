package com.noti.sender.wear;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences",MODE_PRIVATE);
        if(prefs.getBoolean("Enabled",false)) {
            Map<String, String> map = remoteMessage.getData();
            if(map.get("type").equals("send")) {
                sendNotification(map.get("title"), map.get("message"), map.get("package"), map.get("appname"),map.get("device_name"),map.get("device_id"),map.get("date"));
            }
        }
    }

    public void sendNotification(String title, String content, String Package, String AppName,String Device_name,String Device_id,String Date) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(FirebaseMessageService.this, ReceivedActivity.class)
                .putExtra("package", Package)
                .putExtra("device_id",Device_id)
                .putExtra("appname",AppName)
                .putExtra("title",title)
                .putExtra("device_name",Device_name)
                .putExtra("date",Date);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1234, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Package + " " + Date)
                .setContentTitle(title + " (" + AppName + ")")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_notification);
            NotificationChannel channel = new NotificationChannel(Package + " " + Date, AppName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.notify_channel_description));
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        } else builder.setSmallIcon(R.mipmap.ic_notification);

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}

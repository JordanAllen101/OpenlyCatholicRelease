package com.openlycatholic.app.examination;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.openlycatholic.app.R;
import com.openlycatholic.app.MainActivity;

import java.util.ArrayList;



public class PenanceReminderWorker extends Worker {



    private static final String CHANNEL_ID = "penance_reminder_channel";
    private static final String CHANNEL_NAME = "Penance Reminders";
    private static final String CHANNEL_DESCRIPTION = "Reminders for penance";
    private static final String ACTION_COMPLETE = "com.openlycatholic.app.PENANCE_COMPLETE";
    private static final String PREFS_NAME = "ConfessionReminderPrefs";
    private static final String KEY_PENANCE_ENABLED = "penance_enabled";



    public PenanceReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }



    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isPenanceEnabled = prefs.getBoolean(KEY_PENANCE_ENABLED, false);

        if (!isPenanceEnabled) {
            WorkManager.getInstance(context).cancelAllWorkByTag("penance_reminder");
            return Result.success();
        }

        createNotificationChannel(context);
        sendNotification(context);

        return Result.success();
    }

    private String getLatestPenance(Context context) {
        ArrayList<ConfessionRecord> records = ConfessionStorage.getRecords(context);
        if(records != null && !records.isEmpty()) {
            ConfessionRecord latestRecord = records.get(0);
            if(latestRecord.penance != null && !latestRecord.penance.isEmpty()) {
                return latestRecord.penance;
            }
        }
        return null;
    }



    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }



    private void sendNotification(Context context) {
        String latestPenance = getLatestPenance(context);
        String contentText;
        
        if(latestPenance != null && !latestPenance.isEmpty()) {
            contentText = "Your penance: " + latestPenance;
        } else {
            contentText = "Remember to complete your penance";
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent completeIntent = new Intent(ACTION_COMPLETE);
        completeIntent.setPackage(context.getPackageName());
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                completeIntent,
                PendingIntent.FLAG_IMMUTABLE
        );



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Penance Reminder")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(
                        R.mipmap.ic_launcher_foreground,
                        "Complete",
                        completePendingIntent
                );



        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(2, builder.build());
        }
    }

}

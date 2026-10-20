package com.openlycatholic.app.examination;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.work.WorkManager;

public class PenanceCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("com.openlycatholic.app.PENANCE_COMPLETE")) {
                WorkManager.getInstance(context).cancelAllWorkByTag("penance_reminder");
                Toast.makeText(context, "Penance reminder stopped", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

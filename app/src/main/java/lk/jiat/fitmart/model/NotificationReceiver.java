package lk.jiat.fitmart.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        lk.jiat.fitmart.model.NotificationHelper.showNotification(context, title, message);
    }
}
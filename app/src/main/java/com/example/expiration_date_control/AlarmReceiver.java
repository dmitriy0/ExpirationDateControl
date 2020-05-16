package com.example.expiration_date_control;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {

    private Calendar dateTime;
    @Override
    public void onReceive(Context context, Intent intent) {

        dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(System.currentTimeMillis());
        dateTime.set(Calendar.SECOND,0);
        dateTime.set(Calendar.MILLISECOND,0);

        SharedPreferences preferences = getDefaultSharedPreferences(context);
        String title = preferences.getString(dateTime.getTimeInMillis()+"","");

        int id = preferences.getInt(dateTime.getTimeInMillis()+"id",0);

        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0, intent1, 0);

        Intent snoozeIntent = new Intent(context, DeleteReceiver.class);
        snoozeIntent.putExtra("action","delete");
        snoozeIntent.putExtra("number",id);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "chanel")
                .setSmallIcon(R.drawable.ic_arrow)
                .setContentTitle(title)
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent1)
                .addAction(R.drawable.ic_calendar, "Снять", snoozePendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());

    }
}

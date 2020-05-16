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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class DeleteReceiver extends BroadcastReceiver {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Users");
    private Calendar dateTime;
    @Override
    public void onReceive(final Context context, final Intent intent) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancel(intent.getIntExtra("number", -1));

        SharedPreferences preferences = getDefaultSharedPreferences(context);
        final String phoneNumber = preferences.getString("phoneNumber", "");


        if (Objects.equals(intent.getStringExtra("action"), "delete")) {

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    int countProducts = dataSnapshot.child(phoneNumber).child("allProducts").child("count").getValue(Integer.class);
                    myRef.child(phoneNumber).child("allProducts").child("count").setValue(countProducts - 1);
                    myRef.child(phoneNumber).child("allProducts").child(String.valueOf(intent.getIntExtra("number", -1))).removeValue();
                    for (int j = intent.getIntExtra("number", -1) + 1; j < countProducts; j++) {
                        String name = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("name").getValue(String.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("name").setValue(name);

                        String category = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("category").getValue(String.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("category").setValue(category);

                        String count = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("count").getValue(String.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("count").setValue(count);

                        String imagePath = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("imagePath").getValue(String.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("imagePath").setValue(imagePath);

                        String value = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("value").getValue(String.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("value").setValue(value);

                        Long productionDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("productionDate").getValue(Long.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("productionDate").setValue(productionDate);

                        Long notificationDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("notificationDate").getValue(Long.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("notificationDate").setValue(notificationDate);

                        Long validUntilDate = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("validUntilDate").getValue(Long.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("validUntilDate").setValue(validUntilDate);

                        Long notificationTime = dataSnapshot.child(phoneNumber).child("allProducts").child(String.valueOf(j)).child("notificationTime").getValue(Long.class);
                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j - 1)).child("notificationTime").setValue(notificationTime);

                        myRef.child(phoneNumber).child("allProducts").child(String.valueOf(j)).removeValue();

                    }
                    Toast.makeText(context,"Запись удалена",Toast.LENGTH_LONG).show();


                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }

    }
}

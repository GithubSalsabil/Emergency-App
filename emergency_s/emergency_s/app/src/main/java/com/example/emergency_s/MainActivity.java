package com.example.emergency_s;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    TextView coordinates, messagetext;

    // Fixed latitude and longitude values
    String latitude = "35.8579303";
    String longitude = "10.5945139";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        coordinates = findViewById(R.id.coordinates);
        messagetext = findViewById(R.id.messagetext);

        // Display the fixed coordinates
        coordinates.setText("Coordinates :- \n " + latitude + "  " + longitude);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("accident_flag").exists()) {
                    boolean accidentFlag = Boolean.parseBoolean(dataSnapshot.child("accident_flag").getValue().toString());

                    if (!accidentFlag) {
                        messagetext.setTextColor(getResources().getColor(R.color.green));
                        messagetext.setText("STATUS :- \n" + dataSnapshot.child("driversname").getValue().toString() + " is Fine.");
                    } else {
                        messagetext.setTextColor(getResources().getColor(R.color.red));
                        messagetext.setText("STATUS \n" + dataSnapshot.child("driversname").getValue().toString() + " met with an Accident.");
                        promptnotification();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to Get Information!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void promptnotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "some_channel_id";
            CharSequence channelName = "Some Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(1);
            notificationManager.createNotificationChannel(notificationChannel);

            String groupId = "some_group_id";
            CharSequence groupName = "Some Group";
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(groupId, groupName));

            int notifyId = 1;

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, channelId)
                    .setContentTitle("ACCIDENT ALERT")
                    .setContentText("Driver met with an Accident!!!")
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            notificationManager.notify(notifyId, notification.build());
        }
    }

    public void onClickGetAddress(View view) {
        try {
            AddressDialog addressDialog = new AddressDialog(); // No arguments needed
            addressDialog.show(getSupportFragmentManager(), "example dialog");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLocateOnMap(View view) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void onClickNearby(View view) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=hospitals");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void onClickNavigation(View view) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void onClickNearbyps(View view) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=police stations");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}

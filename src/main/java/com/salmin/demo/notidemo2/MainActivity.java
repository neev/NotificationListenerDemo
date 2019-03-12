package com.salmin.demo.notidemo2;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private NotificationManager notiManager;
    private NewNotificationReceiver notiRecever;
    private Button sendNotiButton;
    private Button sendDelayedNotiButton;
    private AlertDialog enableNotificationListenerAlertDialog;
    private NotificationChannel chan1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNotificationManager();
        initAndSetClickListener(); // button to send the notification
        checkNotificationServiceIsEnabled(); // prompt to turn on notification service if needed.
       initAndRegisterReceiver(); // alternative to canceling the notification
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(notiRecever);
    }

    private void initNotificationManager() {
        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initAndSetClickListener() {
        sendNotiButton = (Button) findViewById(R.id.send_noti_button);
        sendDelayedNotiButton = (Button) findViewById(R.id.send_delayed_noti_button);

        sendNotiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: wsa called");
                postNotification();
            }
        });

        sendDelayedNotiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                postNotification();
            }
        });
    }

    private void checkNotificationServiceIsEnabled() {
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     *
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     *
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return (alertDialogBuilder.create());
    }

    private void postNotification() {
        Log.d(TAG, "postNotification: was called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        // create the channel with null vibration, and set it manually before the notification is posted
       Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);

        // build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                "default")
                .setContentTitle("Sample Title")
                .setContentText("sample Body")
                .setVibrate(new long[]{0, 1000, 100, 1000, 100, 1000})
                .setSmallIcon(R.drawable.ic_launcher_background);

        notiManager.notify(1100, builder.build());

    }

    private void createChannel() {
        Log.d(TAG, "createChannel: was called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan1 = new NotificationChannel("default", "Primary Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            chan1.enableVibration(true);
//            chan1.setVibrationPattern(null);
//            chan1.setSound(null, null);
            notiManager.createNotificationChannel(chan1);
        }
    }

    /**
     * this was an alternative attempt at canceling the notification. Broadcast is sent from the
     * NotificationListenerService with the notification id to cancel it.
     */
    private void initAndRegisterReceiver() {
        notiRecever = new NewNotificationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.salmin.demo.notidemo2");
        registerReceiver(notiRecever, intentFilter);
    }

    public class NewNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: from Main was called");
            final int notificationID = intent.getIntExtra("Notification ID", -1);
            Log.d(TAG, "notiManager ID = " + notificationID);

            notiManager.cancel(notificationID);
            notiManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);

        }
    }
}

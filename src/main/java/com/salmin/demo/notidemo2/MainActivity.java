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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

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

        Button cancelNotiButton = (Button) findViewById(R.id.cancel_noti_button);


        // Turn off do not disturb mode, allow all notifications
        cancelNotiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                    int INTERRUPTION_FILTER_ALL
                        Interruption filter constant - Normal interruption
                        filter - no notifications are suppressed.
                */
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
                Toast.makeText(getBaseContext(),"Now off do not disturb mode.",Toast.LENGTH_SHORT).show();

                //mTVStats.setText("Now off do not disturb mode.");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
       unregisterReceiver(notiRecever);
    }

    private void initNotificationManager() {
        notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(!isNotificationServiceEnabled())
        {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
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

            /*// implemented do not disturb for 10 min
            final Timer timer = new Timer();
// Note that timer has been declared final, to allow use in anon. class below
            timer.schedule( new TimerTask()
                            {
                                private int i = 10;
                                public void run()
                                {
                                    System.out.println("30 Seconds Later");
                                    if (--i < 1) timer.cancel(); // Count down ten times, then cancel
                                }
                            }, 30000, 30000 //Note the second argument for repetition
            );*/

            notiManager.cancel(notificationID);
            //notiManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);

            changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);

        }
    }


    protected void changeInterruptionFiler(int interruptionFilter){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){ // If api level minimum 23
            /*
                boolean isNotificationPolicyAccessGranted ()
                    Checks the ability to read/modify notification policy for the calling package.
                    Returns true if the calling package can read/modify notification policy.
                    Request policy access by sending the user to the activity that matches the
                    system intent action ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS.

                    Use ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED to listen for
                    user grant or denial of this access.

                Returns
                    boolean

            */
            // If notification policy access granted for this package
            if(notiManager.isNotificationPolicyAccessGranted()){
                /*
                    void setInterruptionFilter (int interruptionFilter)
                        Sets the current notification interruption filter.

                        The interruption filter defines which notifications are allowed to interrupt
                        the user (e.g. via sound & vibration) and is applied globally.

                        Only available if policy access is granted to this package.

                    Parameters
                        interruptionFilter : int
                        Value is INTERRUPTION_FILTER_NONE, INTERRUPTION_FILTER_PRIORITY,
                        INTERRUPTION_FILTER_ALARMS, INTERRUPTION_FILTER_ALL
                        or INTERRUPTION_FILTER_UNKNOWN.
                */

                // Set the interruption filter
                notiManager.setInterruptionFilter(interruptionFilter);
            }else {
                /*
                    String ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        Activity Action : Show Do Not Disturb access settings.
                        Users can grant and deny access to Do Not Disturb configuration from here.

                    Input : Nothing.
                    Output : Nothing.
                    Constant Value : "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"
                */
                // If notification policy access not granted for this package
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
        else if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){

            //NotificationManagerCompat.from(getBaseContext()).areNotificationsEnabled(‌​);
            Log.d(TAG, "changeInterruptionFiler: cancel NOTIFCATIONS");

            NotificationManagerCompat.from(getApplicationContext()).cancelAll();

        }

    }


}

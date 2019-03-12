package com.salmin.demo.notidemo2;


import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class NotificationListener extends NotificationListenerService {

    public static final int SUPPRESSED_EFFECT_SCREEN_OFF = 1;
    public static final int PRIORITY_CATEGORY_CALLS = 8;
    public static final int PRIORITY_SENDERS_ANY = 0;
    public static final int PRIORITY_CATEGORY_MESSAGES = 4;



    @Override
    public void onListenerConnected() {
        Log.d("NotiDemo2", "onListenerConnected: was called");
        super.onListenerConnected();
        requestListenerHints(HINT_HOST_DISABLE_EFFECTS);

        //requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS);
//        requestInterruptionFilter(INTERRUPTION_FILTER_ALARMS);
    }



    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("NotiDemo2", "onNotificationPosted: was called");
        super.onNotificationPosted(sbn);

       Notification notification = sbn.getNotification();
        Log.d("NotiDemo2", "onNotificationPosted: " + notification);

        try {
            Thread.sleep(50);
            Log.d("NotiDemo2", "sleeping");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }





// to supress screen on
        /*sbn.getNotification().Policy (PRIORITY_CATEGORY_CALLS,
                PRIORITY_SENDERS_ANY,
                PRIORITY_CATEGORY_MESSAGES,
        SUPPRESSED_EFFECT_SCREEN_OFF);
*/
        //to cancel notification
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {

            cancelNotification(sbn.getKey());
        }


        /**
         * this was an alternative attempt at canceling the notification. MainActivity receives
         * this broadcast and cancels the notification from there.
         */
        int notificationID = sbn.getId();
        Intent intent = new Intent("com.salmin.demo.notidemo2");
        intent.putExtra("Notification ID", notificationID);
        Log.d(TAG, "onNotificationPosted: SENT INTENT");
        sendBroadcast(intent);

    }

    /*private long[] getVibrationPattern(StatusBarNotification sbn) {
        *//**
         Default vibrate pattern obtained from:
         https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/notification/NotificationManagerService.java
         **//*
        static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
        try {
            if(sbn.getNotification().defaults == DEFAULT_VIBRATE ||sbn.getNotification().defaults == DEFAULT_ALL) {
                Timber.d("Default vibration pattern is set");
                return DEFAULT_VIBRATE_PATTERN;
            }
            else if(sbn.getNotification().vibrate != null) {
                Timber.d("Custom vibration pattern is set");
                return sbn.getNotification().vibrate;
            }
  catch(Exception e) {
                Timber.d("Error reading defualts");
                return null;
            }
        }

        public void startVibrate(long[] vibrationPattern, boolean isCall, Vibrator v1) {
            try {
                if (isCall) {
                    *//**
                     Effects of calls from certain VoIP apps get curbed upon using:
                     requestListenerHints(HINT_HOST_DISABLE_NOTIFICATION_EFFECTS);
                     As a result, we have to reproduce the effect of the notifications pertaining to the same.
                     We start a vibration with repeat flag set to 0 which means it will play in a loop until that notification is removed.
                     Once the notification is removed, a call to stop vibrate is made.
                     *//*
                    v1.vibrate(new long[]{1500,1000, 2000, 1000},0);
                }
                else{
                    *//**
                     If it's not a call, play the vibration pattern with repeat flag set to -1 which means it will play
                     the vibration pattern only once.
                     *//*
                    if(vibrationPattern.length>0)
                        v1.vibrate(vibrationPattern, -1);
                    else
                        v1.vibrate(new long[]{0, 250, 250, 250}, -1);
                }
            }catch(Exception e){
                Crashlytics.logException(e);
            }
        }

        public void stopVibrate(Vibrator v1) {
            try {
                v1.cancel();
            }catch(Exception e){
                Crashlytics.logException(e);
            }
        }*/


}

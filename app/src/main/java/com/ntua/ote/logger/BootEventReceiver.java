package com.ntua.ote.logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.CallLog;

import com.ntua.ote.logger.db.PropertiesDbHelper;

public class BootEventReceiver extends BroadcastReceiver {

    public static final String TAG = BootEventReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean value = sharedPref.getBoolean(SettingsActivity.KEY_PREF_RUN_ON_START, false);

        if(value) {
            Intent startServiceIntent = new Intent(context, CallLogService.class);
            context.startService(startServiceIntent);
        }
    }
}
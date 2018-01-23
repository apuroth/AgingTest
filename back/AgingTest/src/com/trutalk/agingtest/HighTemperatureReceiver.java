/*
 * Copyright (c) 2015 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 *
 * Not a Contribution.
 * Apache license notifications and license are retained
 * for attribution purposes only.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trutalk.agingtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;

public final class HighTemperatureReceiver extends BroadcastReceiver {
    private static final String TAG = "AgingTest";
    private static final boolean DEBUG = true;
    private static final String TAG_NOTIFICATION = "high_battery_temperature";
    private static final int ID_NOTIFICATION = 100;

    private Context mContext;
    private NotificationManager mNoMan;

    private int mBatteryLevel = 100;
    private int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    private int mPlugType = 0;
    private int mInvalidCharger = 0;
    private int mBatteryTemperature = 0;
    private int mHealth = BatteryManager.BATTERY_HEALTH_UNKNOWN;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receive action: " + intent.getAction());
        mContext = context;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            final int oldBatteryLevel = mBatteryLevel;
            mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
            final int oldBatteryStatus = mBatteryStatus;
            mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            final int oldPlugType = mPlugType;
            mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            final int oldInvalidCharger = mInvalidCharger;
            mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
            final int oldBatteryTemperature = mBatteryTemperature;
            mBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            final int oldHealth = mHealth;
            mHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,
                    BatteryManager.BATTERY_HEALTH_UNKNOWN);


            final boolean plugged = mPlugType != 0;
            final boolean oldPlugged = oldPlugType != 0;

            if (DEBUG) {
                Log.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                Log.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                Log.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                Log.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                Log.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                Log.d(TAG, "temperature    " + oldBatteryTemperature + " --> " + mBatteryTemperature);
                Log.d(TAG, "health         " + oldHealth + " --> " + mHealth);
            }

            boolean needBroadCast = false;
            Intent batteryIntent = new Intent("com.trutalk.agingtest.RECEIVER");
            if (plugged && mHealth > BatteryManager.BATTERY_HEALTH_GOOD) {
                //showBatteryOverTemperatureNotification();
                String health = "";
                switch (mHealth) {
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        health = context.getResources().getString(R.string.battery_heat);
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        health = context.getResources().getString(R.string.battery_dead);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        health = context.getResources().getString(R.string.battery_over);
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        health = context.getResources().getString(R.string.battery_unknown);
                        break;
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        health = context.getResources().getString(R.string.battery_cold);
                        break;
                }
                needBroadCast = true;
                batteryIntent.putExtra("battery_health", health);
            }
            if (needBroadCast || ((mBatteryStatus != oldBatteryStatus) && mBatteryStatus >= BatteryManager.BATTERY_STATUS_CHARGING)) {
                String status = "";
                switch (mBatteryStatus) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        status = context.getResources().getString(R.string.battery_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        status = context.getResources().getString(R.string.battery_discharging);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        status = context.getResources().getString(R.string.battery_not_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        status = context.getResources().getString(R.string.battery_full);
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        status = context.getResources().getString(R.string.battery_status_unknown);
                        break;
                }
                batteryIntent.putExtra("battery_status", status);
                needBroadCast = true;
                //dismissBatteryOverTemperatureNotification();
            }
            if (needBroadCast) {
                batteryIntent.putExtra("battery_temperature", String.valueOf(mBatteryTemperature * 0.1));
                batteryIntent.putExtra("battery_level", String.valueOf(mBatteryLevel));
                context.sendBroadcast(batteryIntent);
            }
        } else {
            Log.d(TAG, "unknown intent: " + intent);
        }
    }
}

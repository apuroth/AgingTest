package com.trutalk.agingtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AgingTestModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent firstIntent = new Intent();
        firstIntent.setClass(context, FirstActivity.class);
        firstIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(firstIntent);
    }
}

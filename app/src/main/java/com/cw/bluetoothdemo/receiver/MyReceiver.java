package com.cw.bluetoothdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cw.bluetoothdemo.activity.BluetoothServerActivity;
import com.cw.bluetoothdemo.activity.MainActivity;

/**
 * Created by yangjinling on 2017/11/29.
 */

public class MyReceiver extends BroadcastReceiver {
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(action_boot)) {
            Intent ootStartIntent = new Intent(context, BluetoothServerActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }
    }
}

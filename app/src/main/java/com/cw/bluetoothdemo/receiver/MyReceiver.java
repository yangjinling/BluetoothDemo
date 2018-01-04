package com.cw.bluetoothdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.cw.bluetoothdemo.activity.BluetoothServerActivity;
import com.cw.bluetoothdemo.activity.MainActivity;
import com.cw.bluetoothdemo.util.Control;

/**
 * Created by yangjinling on 2017/11/29.
 */

public class MyReceiver extends BroadcastReceiver {
    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(action_boot)) {
            Log.e("YJL", "进来了么？？？");
            Intent ootStartIntent = new Intent(context, BluetoothServerActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);

        }
    }
}

package com.cw.bluetoothdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.util.SocketServerUtil;

/**
 * Created by lq on 2017/12/8.
 */

public class WifiService extends Service {
    private SocketServerUtil socketServerUtil;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SocketServerUtil.MESSAGE_SOCKET_READ:
                    String str = msg.obj.toString();
                    Contents.COMMAND_CURRENT = str;
                    Log.e("YJL", "str.length" + str.length());
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();
                    //TODO  此时应该将指令通过串口发出去并从串口中获取内容并传送给客户端
                    sendBroad(Contents.TYPE_WIFI, str);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        socketServerUtil = AppConfig.getInstance().getSocketServerUtil();
        socketServerUtil.registerHandler(mHandler);
        super.onCreate();
    }
    private void sendBroad(String action) {
        sendBroad(action, null);
    }

    private void sendBroad(String action, String string) {
        Intent intent = new Intent(action);
        if (!TextUtils.isEmpty(string)) {
            intent.putExtra(Contents.KEY_WIFI, string);
        }
        sendBroadcast(intent);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("YJL","isClint=="+SocketServerUtil.isClint);
        if (SocketServerUtil.isClint) {
            socketServerUtil.beginListen();
        }
        return super.onStartCommand(intent, flags, startId);
    }

}

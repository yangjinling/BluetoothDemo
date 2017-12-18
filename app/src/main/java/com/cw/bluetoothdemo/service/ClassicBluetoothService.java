package com.cw.bluetoothdemo.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.util.BoxDataUtils;

/**
 * Created by yangjinling on 2017/11/28.
 */

public class ClassicBluetoothService extends Service {
    private BluetoothChatUtil mBlthChatUtil;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.ACCEPT_SUCCESS:
                    //服务端服务开启成功
                    //accep成功
                    sendBroad(Contents.CONNECT_SUCCESS);
                    break;
                case BluetoothChatUtil.STATE_CONNECTED:
                    //连接到的设备名称
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    Log.e("YJL", "deviceName==" + deviceName);
                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:
                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    sendBroad(Contents.CONNECT_FAIL);
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:
                    //客户端断开连接
                    mBlthChatUtil.disconnect();
                    mHandler.sendEmptyMessageDelayed(BluetoothChatUtil.STATE_LISTEN, 10);
                    break;
                case BluetoothChatUtil.STATE_LISTEN:
                    //客户端断开连接后重新监听
                    mBlthChatUtil.startListen();
                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                    //读取到客户端指令并传给ui界面
                    String str = msg.getData().getString(BluetoothChatUtil.READ_MSG);
                    Contents.COMMAND_CURRENT = str;
                    Log.e("YJL", "str.length" + str.length());
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();
                    //TODO  此时应该将指令通过串口发出去并从串口中获取内容并传送给客户端
                    sendBroad(Contents.TYPE_BLUE, str);
                    break;
                }
                case BluetoothChatUtil.MESSAGE_WRITE: {
                    byte[] buf = (byte[]) msg.obj;
//                    String str = new String(buf, 0, buf.length);
                    //发送成功的提示可有可无
                    String str = BJCWUtil.HexTostr(buf, buf.length);
                    Toast.makeText(getApplicationContext(), "发送成功" + str, Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }

    };

    private void sendBroad(String action) {
        sendBroad(action, null);
    }

    private void sendBroad(String action, String string) {
        Intent intent = new Intent(action);
        if (!TextUtils.isEmpty(string)) {
            intent.putExtra(Contents.KEY_BLUE, string);
        }
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBlthChatUtil = AppConfig.getInstance().getmBluetoothChatUtil();
        mBlthChatUtil.registerHandler(mHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mBlthChatUtil != null) {
            // 只有国家是state_none，我们知道，我们还没有开始
            if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_NONE) {
                // 启动蓝牙聊天服务
                mBlthChatUtil.startListen();
            } else if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
                if (null != device && null != device.getName()) {
                    Log.e("YJL", "已成功连接到设备" + device.getName());
                } else {
                    Log.e("YJL", "已成功连接到设备");
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}

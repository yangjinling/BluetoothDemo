package com.cw.bluetoothdemo.app;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.cw.bluetoothdemo.connection.SerialConnection;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.util.SocketServerUtil;
import com.skzh.iot.SerialPort;

/**
 * Created by yangjinling on 2017/11/29.
 */
public class AppConfig {

    private static AppConfig mAppConfig;
    /**
     * 全局获取Context
     */
    private static Context context;
    private SerialConnection connection;
    private BluetoothChatUtil mBluetoothChatUtil;
    private static SerialPort mSerialPort = null;
    private BluetoothGattServer server;
    private BluetoothManager manager;
    private BluetoothGattCharacteristic character;
    private BluetoothGattService service;
    private SocketServerUtil socketServerUtil;
    private AppConfig() {
    }

    //单例
    public static AppConfig getInstance() {
        // 先检查实例是否存在，如果不存在才进入下面的同步块
        if (mAppConfig == null) {
            // 同步块，线程安全的创建实例
            synchronized (AppConfig.class) {
                // 再次检查实例是否存在，如果不存在才真正的创建实例
                if (mAppConfig == null) {
                    mAppConfig = new AppConfig();
                }
            }
        }
        return mAppConfig;
    }

    public SocketServerUtil getSocketServerUtil() {
        return socketServerUtil;
    }

    public void setSocketServerUtil(SocketServerUtil socketServerUtil) {
        this.socketServerUtil = socketServerUtil;
    }

    public BluetoothGattServer getServer() {
        return server;
    }

    public void setServer(BluetoothGattServer server) {
        this.server = server;
    }

    public BluetoothGattCharacteristic getCharacter() {
        return character;
    }

    public void setCharacter(BluetoothGattCharacteristic character) {
        this.character = character;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothChatUtil getmBluetoothChatUtil() {
        return mBluetoothChatUtil;
    }

    public void setmBluetoothChatUtil(BluetoothChatUtil mBluetoothChatUtil) {
        this.mBluetoothChatUtil = mBluetoothChatUtil;
    }

    public SerialConnection getConnection() {
        return connection;
    }

    public void setConnection(SerialConnection connection) {
        this.connection = connection;
    }

    public static SerialPort getmSerialPort() {
        return mSerialPort;
    }

    public static void setmSerialPort(SerialPort mSerialPort) {
        AppConfig.mSerialPort = mSerialPort;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AppConfig.context = context;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
        }
        mSerialPort = null;
    }


    /**
     * 重新打开串口
     */
    public void reStartSerial() {
        closeSerialPort();
        connection = new SerialConnection(context);
    }
}

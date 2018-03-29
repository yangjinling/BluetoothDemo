package com.cw.bluetoothdemo.app;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.text.TextUtils;
import android.util.Log;

import com.cw.bluetoothdemo.connection.SerialConnection;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.util.Control;
import com.cw.bluetoothdemo.util.PrefUtils;
import com.cw.bluetoothdemo.util.SocketServerUtil;
import com.wellcom.finger.FpDriverV12;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by yangjinling on 2017/11/29.
 */

public class MyApplication extends Application {
    private BluetoothGattCharacteristic character;
    private BluetoothGattService service;
    private SocketServerUtil socketServerUtil;
    private FpDriverV12 mIFpDevDriver;

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.setContext(getApplicationContext());
//        Control.gpio_control(927, 1);//串口
      /*  if (!PrefUtils.getBoolean(AppConfig.getContext(), "WAKE", true)) {
            Control.gpio_control(927, 1);//串口
            Control.gpio_control(921, 1);//host3
            Control.gpio_control(920, 1);//host4
            Control.gpio_control(922, 1);//host1
            Control.gpio_control(1006, 1);//host2
            Control.gpio_control(1010, 1);//hub
            Control.gpio_control(1009, 1);//hub
            Control.gpio_control(969, 1);//切换host
        }*/
      /*  Control.gpio_control(927, 1);//串口
        Control.gpio_control(921, 1);//host3
        Control.gpio_control(920, 1);//host4
        Control.gpio_control(922, 1);//host1
        Control.gpio_control(1006, 1);//host2
        Control.gpio_control(1010, 1);//hub
        Control.gpio_control(1009, 1);//hub
        Control.gpio_control(969, 1);//切换host*/
        AppConfig.getInstance().setConnection(new SerialConnection(AppConfig.getContext()));
        AppConfig.getInstance().setmBluetoothChatUtil(BluetoothChatUtil.getInstance(getApplicationContext()));
        character = new BluetoothGattCharacteristic(
                UUID.fromString(Contents.characteristicUUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
//        character.addDescriptor(new BluetoothGattDescriptor(CCCD, BluetoothGattDescriptor.PERMISSION_WRITE));
        service = new BluetoothGattService(UUID.fromString(Contents.serviceUUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        service.addCharacteristic(character);
        AppConfig.getInstance().setCharacter(character);
        AppConfig.getInstance().setService(service);
        socketServerUtil = new SocketServerUtil(9999);
        AppConfig.getInstance().setSocketServerUtil(socketServerUtil);
    }
}

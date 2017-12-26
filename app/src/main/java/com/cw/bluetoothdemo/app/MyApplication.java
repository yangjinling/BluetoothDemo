package com.cw.bluetoothdemo.app;

import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;

import com.cw.bluetoothdemo.connection.SerialConnection;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.util.SocketServerUtil;
import com.wellcom.finger.FpDriverV12;

import java.util.UUID;

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
        try {
            mIFpDevDriver = new FpDriverV12(getApplicationContext());// finger print driver
            AppConfig.getInstance().setmIFpDevDriver(mIFpDevDriver);
            mIFpDevDriver.openDevice();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

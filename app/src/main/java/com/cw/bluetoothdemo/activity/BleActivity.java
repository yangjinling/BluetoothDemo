package com.cw.bluetoothdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cw.bluetoothdemo.R;
import com.cw.bluetoothdemo.service.BleService;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BoxDataUtils;

import java.util.UUID;

public class BleActivity extends AppCompatActivity {


    public static String serviceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    public static String characteristicUUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    public static UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    ;
    private BluetoothGattServer server;
    private BluetoothManager manager;
    private BluetoothGattCharacteristic character;
    private BluetoothGattService service;
    private BluetoothDevice mDevice;

    TextView info;
    String infoStr = "";
    private Handler mHandler = new Handler();
    private StringBuilder builder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


     /*   character = new BluetoothGattCharacteristic(
                UUID.fromString(characteristicUUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
//        character.addDescriptor(new BluetoothGattDescriptor(CCCD, BluetoothGattDescriptor.PERMISSION_WRITE));
        service = new BluetoothGattService(UUID.fromString(serviceUUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        service.addCharacteristic(character);

        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        server = manager.openGattServer(this,
                new BluetoothGattServerCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothDevice device,
                                                        int status, int newState) {
                        ShowInfo("Chris", "onConnectionStateChange");
                        super.onConnectionStateChange(device, status, newState);
                        mDevice = device;
                    }

                    @Override
                    public void onServiceAdded(int status,
                                               BluetoothGattService service) {

                        ShowInfo("Chris", "service added");
                        super.onServiceAdded(status, service);
                    }

                    @Override
                    public void onCharacteristicReadRequest(
                            BluetoothDevice device, int requestId, int offset,
                            BluetoothGattCharacteristic characteristic) {
                        ShowInfo("Chris", "onCharacteristicReadRequest");
                        super.onCharacteristicReadRequest(device, requestId,
                                offset, characteristic);
                    }

                    @Override
                    public void onCharacteristicWriteRequest(
                            final BluetoothDevice device, int requestId,
                            final BluetoothGattCharacteristic characteristic,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, byte[] value) {
                        ShowInfo("Chris", "onCharacteristicWriteRequest");
//                        super.onCharacteristicWriteRequest(device, requestId,
//                                characteristic, preparedWrite, responseNeeded,
//                                offset, value);
                        mDevice = device;
                        final String code = BJCWUtil.HexTostr(value, value.length);
                        builder = new StringBuilder();
                        Log.i("YJL", "客户端发过来的数据:+commandlength===" + code.length() + "客户端发过来的数据:+command===" + code);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BoxDataUtils.getData(code, new BoxDataUtils.DataCallBack() {
                                    @Override
                                    public void onSuccess(String s) {
                                        builder.append(s);
                                        Log.e("YJL", "服务器准备向客户端发送数据" + builder.toString().length());
                                        String strSW = s.substring(s.length() - 4);
                                        int pulSW = Integer.valueOf(strSW, 16);
                                        Log.e("YJL", "pulSw===" + pulSW);
                                        if (pulSW == 0x9000) {
                                            dealDate(BJCWUtil.StrToHex(builder.toString().trim()), device, character);
                                        }
                                    }

                                    @Override
                                    public void onFail() {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onDescriptorReadRequest(BluetoothDevice device,
                                                        int requestId, int offset,
                                                        BluetoothGattDescriptor descriptor) {
                        ShowInfo("Chris", "onDescriptorReadRequest");
                        super.onDescriptorReadRequest(device, requestId,
                                offset, descriptor);
                    }

                    @Override
                    public void onDescriptorWriteRequest(
                            BluetoothDevice device, int requestId,
                            BluetoothGattDescriptor descriptor,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, byte[] value) {
                        ShowInfo("Chris", "onDescriptorWriteRequest");
                        super.onDescriptorWriteRequest(device, requestId,
                                descriptor, preparedWrite, responseNeeded,
                                offset, value);
                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device,
                                               int requestId, boolean execute) {
                        ShowInfo("Chris", "onExecuteWrite");
                        super.onExecuteWrite(device, requestId, execute);
                    }

                });

        server.addService(service);*/

        this.setContentView(R.layout.activity_ble_activity);

        super.onCreate(savedInstanceState);

        info = (TextView) findViewById(R.id.textView3);
        Intent intent = new Intent(BleActivity.this, BleService.class);
        startService(intent);

    }

    int i = 0;

    public void onButtonClicked(View v) {
        /*ShowInfo("Chris", "XXXX");
        i++;
        character.setValue("index" + i);
        server.addService(service);*/
    }


    void ShowInfo(String st, String s) {
        //infoStr  = infoStr + s  + "\n";
        //info.setText(infoStr);

        Log.i("BT", s);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(BleActivity.this, BleService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void dealDate(byte[] data, final BluetoothDevice mDevice,
                         final BluetoothGattCharacteristic characters) {
        if (data != null) {
            int index = 0;
            do {
                try {
                    byte[] surplusData = new byte[data.length - index];
                    byte[] currentData;
                    System.arraycopy(data, index, surplusData, 0, data.length - index);
                    if (surplusData.length <= 20) {
                        currentData = new byte[surplusData.length];
                        System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
                        index += surplusData.length;
                    } else {
                        currentData = new byte[20];
                        System.arraycopy(data, index, currentData, 0, 20);
                        index += 20;
                    }
                    character.setValue(currentData);
                    server.notifyCharacteristicChanged(mDevice, character, false);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (index < data.length);
        }
    }
}

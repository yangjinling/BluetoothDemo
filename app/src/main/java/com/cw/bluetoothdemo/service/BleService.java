package com.cw.bluetoothdemo.service;

import android.app.Service;
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
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;

import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BoxDataUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lq on 2017/12/8.
 */

public class BleService extends Service {
    private BluetoothGattServer server;
    private BluetoothManager manager;
    private BluetoothGattCharacteristic character;
    private BluetoothGattService service;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };
    private StringBuilder builder = new StringBuilder();
    private StringBuilder stringBuilder = new StringBuilder();
    private int contactIndex;//记录接触第index+1指令
    private int noContactIndex;//记录非接触第index+1指令

    @Override
    public void onCreate() {
        super.onCreate();
        character = AppConfig.getInstance().getCharacter();
        service = AppConfig.getInstance().getService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String cmd;

    private int code = 10;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        server = manager.openGattServer(this,
                new BluetoothGattServerCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothDevice device,
                                                        int status, int newState) {
                        Log.e("Chris", "onConnectionStateChange");
                        super.onConnectionStateChange(device, status, newState);
                    }

                    @Override
                    public void onServiceAdded(int status,
                                               BluetoothGattService service) {

                        Log.e("Chris", "service added");
                        super.onServiceAdded(status, service);
                    }

                    @Override
                    public void onCharacteristicReadRequest(
                            BluetoothDevice device, int requestId, int offset,
                            BluetoothGattCharacteristic characteristic) {
                        Log.e("Chris", "onCharacteristicReadRequest");
                        super.onCharacteristicReadRequest(device, requestId,
                                offset, characteristic);
                    }

                    @Override
                    public void onCharacteristicWriteRequest(
                            final BluetoothDevice device, int requestId,
                            final BluetoothGattCharacteristic characteristic,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, byte[] value) {
                        Log.e("Chris", "onCharacteristicWriteRequest");
//                        super.onCharacteristicWriteRequest(device, requestId,
//                                characteristic, preparedWrite, responseNeeded,
//                                offset, value);
                        cmd = BJCWUtil.HexTostr(value, value.length);
                        builder = new StringBuilder();
                        Contents.COMMAND_CURRENT = cmd;
                       /* if (cmd.startsWith("0")) {
                            code = Integer.parseInt(cmd.substring(0, 2));
                            cmd = cmd.substring(2);
                        } else {
                            code = 10;
                        }*/
                        Log.e("YJL", "客户端发过来的数据:+commandlength===" + cmd.length() + "客户端发过来的数据:+command===" + cmd);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                               /* if (code != 10) {
                                    sendBroad(Contents.TYPE_BLE, code);
                                }*/
                                sendBroad(Contents.COMMAND_CODE, cmd);
                                BoxDataUtils.getData(cmd/*.substring(2)*/, new BoxDataUtils.DataCallBack() {
                                    @Override
                                    public void onSuccess(String s) {
                                        if ("2A".equals(s)) {
                                            Log.e("YJL", "s==" + s + BJCWUtil.StrToHex("2A") + new String(BJCWUtil.StrToHex("2A")));
                                            stringBuilder.append(new String(BJCWUtil.StrToHex(s)));
                                            sendBroad(Contents.TYPE_BLE, stringBuilder.toString());
                                        } else if ("08".equals(s)) {
                                            Log.e("YJL", "s==" + s);
                                            if (stringBuilder.toString().length() > 0) {
                                                String result = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
                                                sendBroad(Contents.TYPE_BLE, result);
                                                stringBuilder = new StringBuilder();
                                                stringBuilder.append(result);
                                            } else {
                                                stringBuilder = new StringBuilder();
                                            }
                                        } else {
                                            if (!cmd.startsWith("F5") && cmd.startsWith("F0")) {
                                                //查看是否明文密文指令
                                                sendBroad(Contents.TYPE_BLE, "");
                                            }
                                            stringBuilder = new StringBuilder();
                                            builder.append(s);
                                            Log.e("YJL", "服务器准备向客户端发送数据" + builder.toString().length());
                                            String strSW = s.substring(s.length() - 4);
                                            int pulSW = Integer.valueOf(strSW, 16);
                                            Log.e("YJL", "pulSw===" + pulSW);
                                            boolean completion = BJCWUtil.judgeData(builder.toString());
                                            if (completion) {
                                                dealDate(BJCWUtil.StrToHex(builder.toString().trim()), device, character);
                                                if (pulSW == 0x9000) {
                                                } else {
                                                    builder = new StringBuilder();
                                                }
                                            }
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
                        Log.e("Chris", "onDescriptorReadRequest");
                        super.onDescriptorReadRequest(device, requestId,
                                offset, descriptor);
                    }

                    @Override
                    public void onDescriptorWriteRequest(
                            BluetoothDevice device, int requestId,
                            BluetoothGattDescriptor descriptor,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, byte[] value) {
                        Log.e("Chris", "onDescriptorWriteRequest");
                        super.onDescriptorWriteRequest(device, requestId,
                                descriptor, preparedWrite, responseNeeded,
                                offset, value);
                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device,
                                               int requestId, boolean execute) {
                        Log.e("Chris", "onExecuteWrite");
                        super.onExecuteWrite(device, requestId, execute);
                    }

                });

        server.addService(service);
        AppConfig.getInstance().setServer(server);
        return super.onStartCommand(intent, flags, startId);
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
                    Thread.sleep(100);
                    character.setValue(currentData);
                    server.notifyCharacteristicChanged(mDevice, character, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } while (index < data.length);
        }
    }

    private void sendBroad(String action) {
        sendBroad(action, null);
    }

    private void sendBroad(String action, String string) {
        Intent intent = new Intent(action);
        if (!TextUtils.isEmpty(string)) {
            intent.putExtra(Contents.KEY_BLE, string);
        }
        sendBroadcast(intent);
    }

    private void sendBroad(String action, int value) {
        Intent intent = new Intent(action);
        intent.putExtra(Contents.COMMAND_CODE, value);
        sendBroadcast(intent);
    }
}

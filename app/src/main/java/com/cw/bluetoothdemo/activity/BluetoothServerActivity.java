package com.cw.bluetoothdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.bluetoothdemo.R;
import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.manager.MediaServiceManager;
import com.cw.bluetoothdemo.service.BleService;
import com.cw.bluetoothdemo.service.ClassicBluetoothService;
import com.cw.bluetoothdemo.service.WifiService;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BoxDataUtils;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BluetoothServerActivity extends Activity {
    private final static String TAG = "BluetoothServerActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Context mContext;
    private Button delete;
    private TextView command;
    private StringBuilder stringBuilder;
    private StringBuilder resultBuilder;
    private ListView lv;
    private List<String> lists = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String cmd;
    private BluetoothDevice mDevice;
    private LinearLayout prograss;
    private TextView prograss_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_bluetooth);
        mContext = this;
        stringBuilder = new StringBuilder();
        resultBuilder = new StringBuilder();
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, lists);
        //初始化控件
        initView();
        //初始化蓝牙
        initBluetooth();
        //注册广播且处理数据
        register();
    }

    private void register() {
        Log.e("YJL", "注册方法");
        IntentFilter filter = new IntentFilter();
        //服务开启失败
        filter.addAction(Contents.CONNECT_FAIL);
        //服务开启成功
        filter.addAction(Contents.CONNECT_SUCCESS);
        //经典蓝牙
        filter.addAction(Contents.TYPE_BLUE);
        //ble蓝牙
        filter.addAction(Contents.TYPE_BLE);
        //wifi
        filter.addAction(Contents.TYPE_WIFI);
        registerReceiver(receiver, filter);
    }

    //初始化控件
    private void initView() {
        lv = ((ListView) findViewById(R.id.lv));
        lv.setAdapter(adapter);
        delete = ((Button) findViewById(R.id.delete));
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lists.clear();
                adapter.notifyDataSetChanged();
                command.setText("");
            }
        });
        command = ((TextView) findViewById(R.id.command));
        prograss = ((LinearLayout) findViewById(R.id.layout_prograss));
        prograss_tv = ((TextView) findViewById(R.id.prograss_tv));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (Contents.CONNECT_FAIL.equals(action)) {
                Toast.makeText(getApplicationContext(), "服务开启失败，请重新开启", Toast.LENGTH_SHORT).show();
            } else if (Contents.CONNECT_SUCCESS.equals(action)) {
                Toast.makeText(getApplicationContext(), "服务已开启", Toast.LENGTH_SHORT).show();
            } else if (Contents.TYPE_BLUE.equals(action)) {
                //经典蓝牙
                dealMessage(intent, true, false);
            } else if (Contents.TYPE_BLE.equals(action)) {
                //ble蓝牙
                dealMessage(intent, true, true);
            } else if (Contents.TYPE_WIFI.equals(action)) {
                //wifi
                dealMessage(intent, false, false);
            }
        }
    };

    /**
     * 播放语音，串口发送接收并返回结果
     * blueOrWifi:蓝牙或wifi
     * ble:经典蓝牙或ble蓝牙
     */
    private void dealMessage(final Intent intent, boolean blueOrWifi, boolean ble) {
        if (blueOrWifi) {
            //true：蓝牙  false：wifi
            if (ble) {
                //true ble蓝牙
                cmd = intent.getStringExtra(Contents.KEY_BLE);
                mDevice = intent.getParcelableExtra(Contents.KEY_DEVICE);
            } else {
                //false  经典蓝牙
                cmd = intent.getStringExtra(Contents.KEY_BLUE);
            }
        } else {
            //wifi
            cmd = intent.getStringExtra(Contents.KEY_WIFI);
        }
        lists.add(cmd);
        adapter.notifyDataSetChanged();
        lv.setSelection(lists.size() - 1);
        //播放语音，根据指令设置
        playVoice(cmd);
        //串口发送指令并处理
        sendMessagBySerialPort(cmd, blueOrWifi, ble);
    }

    /**
     * 串口发送指令
     * cmd：指令
     * blueOrWifi：蓝牙或者wifi
     * ble：经典蓝牙或者ble蓝牙
     */
    private void sendMessagBySerialPort(final String cmd, final boolean blueOrWifi, final boolean ble) {
        BoxDataUtils.getData(cmd, new BoxDataUtils.DataCallBack() {
            @Override
            public void onSuccess(final String data) {
                Log.e("YJL", "data==" + data);
                            /*如果是明文或者密文指令*/
                if ("2A".equals(data)) {
                    /*数字键*/
                    if (Contents.play_KeyMi) {
                        Contents.play_KeyMi = false;
                    } else if (Contents.play_KeyMing) {
                        Contents.play_KeyMing = false;
                    }
                    prograss.setVisibility(View.GONE);
                    stringBuilder.append(new String(BJCWUtil.StrToHex(data)));
                    command.setText(stringBuilder.toString());
                } else if ("08".equals(data)) {
                    /*清除键*/
                    if (Contents.play_KeyMi) {
                        Contents.play_KeyMi = false;
                    } else if (Contents.play_KeyMing) {
                        Contents.play_KeyMing = false;
                    }
                    prograss.setVisibility(View.GONE);
                    if (stringBuilder.toString().length() > 0) {
                        command.setText(stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1));
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(command.getText().toString());
                    } else {
                        stringBuilder = new StringBuilder();
                    }
                } else {
                    if (Contents.play_KeyMi || Contents.play_KeyMing) {
                        //查看是否明文密文指令
                    } else {
                        command.setText("");
                    }
                    resultBuilder.append(data);
                    stringBuilder = new StringBuilder();
                    String result = resultBuilder.toString();
                    if (resultBuilder.length() >= 10) {
                        String strSW = resultBuilder.substring(resultBuilder.length() - 4);
                        int pulSW = Integer.valueOf(strSW, 16);
                        Log.e("YJL", "result===" + result);
                        Log.e("YJL", "pulSw===" + pulSW);
                        /*判断数据是否完整*/
                        boolean completion = BJCWUtil.judgeData(result);
                        if (completion) {
                            //设置当前prograss是否隐藏
                            setPrograssShow();
                            /*状态码显示*/
                            command.setText("" + strSW);
                            if (blueOrWifi) {
                                //true：蓝牙
                                if (ble) {
                                    //true ble蓝牙
                                    if (null != mDevice)
                                        dealDate(BJCWUtil.StrToHex(result), mDevice, AppConfig.getInstance().getCharacter(), AppConfig.getInstance().getServer());
                                } else {
                                    //false 经典蓝牙
                                    AppConfig.getInstance().getmBluetoothChatUtil().write(BJCWUtil.StrToHex(result));
                                }
                            } else {
//                                false：wifi
                                AppConfig.getInstance().getSocketServerUtil().sendMessage(BJCWUtil.StrToHex(result));
                            }
                            resultBuilder = new StringBuilder();
                        } else {
//                            数据不完整，重新发送
                            resultBuilder = new StringBuilder();
                            sendMessagBySerialPort(cmd, blueOrWifi, ble);
                        }
                    } else {
                        //数据长度不正确重新发送
                        resultBuilder = new StringBuilder();
                        //重发
                        sendMessagBySerialPort(cmd, blueOrWifi, ble);
                    }
                }
            }

            @Override
            public void onFail() {
                //重发
                resultBuilder = new StringBuilder();
                sendMessagBySerialPort(cmd, blueOrWifi, ble);
            }
        });
    }

    /**
     * ble蓝牙分包处理发送
     * data：完整的包数据
     * mDevice：蓝牙设备
     */
    public void dealDate(byte[] data, final BluetoothDevice mDevice,
                         BluetoothGattCharacteristic character, BluetoothGattServer server) {
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

    /**
     * 根据指令类型设置语音播放内容
     */
    private void playVoice(String s) {
        Log.e("YJL", "command===" + s);
        switch (s) {
            //获取版本指令
            case Contents.COMMAND_VERSION:
                command.setText("");
                prograss.setVisibility(View.VISIBLE);
                prograss_tv.setText("获取固件版本");
                break;
            //IC_接触卡指令
            case Contents.COMMAND_IC_CONTACT_1:
                command.setText("");
                if (!Contents.play_Contact) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请插卡");
                    setPlayVoice(1);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_BANKCARD1, null);
                }
                break;
            //IC_非接触卡指令
            case Contents.COMMAND_IC_NOCONTACT_1:
                command.setText("");
                if (!Contents.play_NoContact) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请挥卡");
                    setPlayVoice(2);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_BANKCARD2, null);
                }
                break;
            //身份证指令
            case Contents.COMMAND_IDCARD_1:
                command.setText("");
                if (!Contents.play_Idcard) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请放身份证");
                    setPlayVoice(3);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_IDCARD, null);
                }
                break;
            //磁条卡指令
            case Contents.COMMAND_MAGNETIC:
                command.setText("");
                if (!Contents.play_Magnetic) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请刷磁条卡");
                    setPlayVoice(4);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_MAGNETIC, null);
                }
                break;
            //明文指令
            case Contents.COMMAND_PIN_PLAINTEXT:
                command.setText("");
                if (!Contents.play_KeyMing) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按键");
                    setPlayVoice(5);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_KEY, null);
                }
                break;
            //密文指令
            case Contents.COMMAND_PIN_CIPHERTEXT_1:
                command.setText("");
                if (!Contents.play_KeyMi) {
                    Contents.COMMAND_CURRENT = Contents.COMMAND_PIN_CIPHERTEXT_1;
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按键");
                    setPlayVoice(6);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_KEY, null);
                }
                break;

            //指纹指令
            case Contents.COMMAND_FINGER:
                command.setText("");
                if (!Contents.play_Finger) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按指纹");
                    setPlayVoice(7);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_FINGER, null);
                }
                break;
            //执行刷卡结束的指令后播放标记置为false
            case Contents.COMMAND_IC_END:
                setPlayVoice(8);

                break;
            //执行身份证结束的指令后播放标记置为false
            case Contents.COMMAND_IDCARD_2:
                setPlayVoice(8);
                break;
        }
    }

    /**
     * 控制设置prograss显示
     */
    private void setPrograssShow() {
        if (Contents.COMMAND_CURRENT.equals(Contents.COMMAND_PIN_CIPHERTEXT_1)) {
            //密文指令第一条不做任何处理
        } else if (Contents.COMMAND_CURRENT.equals(Contents.COMMAND_IC_END)
                || Contents.COMMAND_CURRENT.equals(Contents.COMMAND_IDCARD_2)
                || Contents.COMMAND_CURRENT.equals(Contents.COMMAND_VERSION)) {
            //身份证最后一条指令/刷卡最后一条指令/获取版本
            prograss.setVisibility(View.GONE);
        } else {
            if (Contents.play_Magnetic) {
                prograss.setVisibility(View.GONE);
                Contents.play_Magnetic = false;
            }
            if (Contents.play_Finger) {
                prograss.setVisibility(View.GONE);
                Contents.play_Finger = false;
            }
            if (Contents.play_KeyMi) {
                prograss.setVisibility(View.GONE);
                Contents.play_KeyMi = false;
            }
            if (Contents.play_KeyMing) {
                prograss.setVisibility(View.GONE);
                Contents.play_KeyMing = false;
            }
        }
    }


    /**
     * 设置语音播放是否完成的标记
     */
    private void setPlayVoice(int type) {
        Contents.play_Contact = false;
        Contents.play_NoContact = false;
        Contents.play_Idcard = false;
        Contents.play_Magnetic = false;
        Contents.play_KeyMing = false;
        Contents.play_KeyMi = false;
        Contents.play_Finger = false;
        switch (type) {
            case 1:
                //接触
                Contents.play_Contact = true;
                break;
            case 2:
                //非接触
                Contents.play_NoContact = true;
                break;
            case 3:
                //身份证
                Contents.play_Idcard = true;
                break;
            case 4:
                //磁条卡
                Contents.play_Magnetic = true;
                break;
            case 5:
                //明文密码
                Contents.play_KeyMing = true;
                break;
            case 6:
                //密文密码
                Contents.play_KeyMi = true;
                break;
            case 7:
                //指纹
                Contents.play_Finger = true;
                break;
        }
    }

    /**
     * 初始化蓝牙设备
     */
    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {//设备不支持蓝牙
            Log.e("YJL", " \"设备不支持蓝牙\"");
            Toast.makeText(getApplicationContext(), "设备不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //判断蓝牙是否开启
        if (!mBluetoothAdapter.isEnabled()) {
            //蓝牙未开启
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//            mBluetoothAdapter.enable();//此方法直接开启蓝牙，不建议这样用。
            Log.e("YJL", mBluetoothAdapter.getAddress() + mBluetoothAdapter.getName());
        }
        //设置蓝牙可见性
        if (mBluetoothAdapter.isEnabled()) {
            Log.e("YJL", "mode==" + mBluetoothAdapter.getScanMode());
//            if (mBluetoothAdapter.getScanMode() ==
//                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            setDiscoverableTimeout(300);
            Intent intent1 = new Intent(BluetoothServerActivity.this, ClassicBluetoothService.class);
            startService(intent1);
            Intent intent2 = new Intent(BluetoothServerActivity.this, BleService.class);
            startService(intent2);
            Intent intent3 = new Intent(BluetoothServerActivity.this, WifiService.class);
            startService(intent3);
            Log.e("YJLs", mBluetoothAdapter.getAddress() + mBluetoothAdapter.getName());
//            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult request=" + requestCode + " result=" + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //后台修改蓝牙的可见性
                setDiscoverableTimeout(300);
                Intent intent1 = new Intent(BluetoothServerActivity.this, ClassicBluetoothService.class);
                startService(intent1);
                Intent intent2 = new Intent(BluetoothServerActivity.this, BleService.class);
                startService(intent2);
                Intent intent3 = new Intent(BluetoothServerActivity.this, WifiService.class);
                startService(intent3);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        Intent intent1 = new Intent(BluetoothServerActivity.this, ClassicBluetoothService.class);
        stopService(intent1);
        Intent intent2 = new Intent(BluetoothServerActivity.this, BleService.class);
        stopService(intent2);
        Intent intent3 = new Intent(BluetoothServerActivity.this, WifiService.class);
        stopService(intent3);
        if (null != AppConfig.getInstance().getServer() && null != AppConfig.getInstance().getServer().getServices() && 0 != AppConfig.getInstance().getServer().getServices().size()) {
            AppConfig.getInstance().getServer().clearServices();
        }
        if (null != AppConfig.getInstance().getServer())
            AppConfig.getInstance().getServer().close();
        AppConfig.getInstance().getmBluetoothChatUtil().unregisterHandler();
        AppConfig.getInstance().getmBluetoothChatUtil().disconnect();
        AppConfig.getInstance().getSocketServerUtil().unregisterHandler();
        AppConfig.getInstance().getSocketServerUtil().disconnect();
        MediaServiceManager.stopService(BluetoothServerActivity.this);
        AppConfig.getInstance().closeSerialPort();
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    /**
     * 开启的可见性，还有个附件的属性，timeout值并没有起到作用，可见性是一直保持的
     */
    public void setDiscoverableTimeout(int timeout) {
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(mBluetoothAdapter, timeout);
            setScanMode.invoke(mBluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭可见性
     */
    public void closeDiscoverableTimeout() {
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(mBluetoothAdapter, 1);
            setScanMode.invoke(mBluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);

        } catch
                (Exception e) {

            e.printStackTrace();

        }

    }
}

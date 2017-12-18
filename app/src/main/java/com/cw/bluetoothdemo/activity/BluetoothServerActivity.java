package com.cw.bluetoothdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.bluetoothdemo.R;
import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.service.BleService;
import com.cw.bluetoothdemo.service.ClassicBluetoothService;
import com.cw.bluetoothdemo.service.WifiService;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BluetoothChatUtil;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BoxDataUtils;

import java.lang.reflect.Method;

public class BluetoothServerActivity extends Activity implements OnClickListener {
    private final static String TAG = "BluetoothServerActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Context mContext;

    private Button mBtnBluetoothVisibility;
    private Button mBtnBluetoohDisconnect;
    private Button mBtnSendMessage;
    private EditText mEdttMessage;

    private TextView mBtConnectState;
    private TextView mTvChat;
    private ProgressDialog mProgressDialog;
    private BluetoothChatUtil mBlthChatUtil;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothChatUtil.STATE_CONNECTED:
                    String deviceName = msg.getData().getString(BluetoothChatUtil.DEVICE_NAME);
                    mBtConnectState.setText("已成功连接到设备" + deviceName);
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    break;
                case BluetoothChatUtil.STATAE_CONNECT_FAILURE:
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothChatUtil.MESSAGE_DISCONNECTED:
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    mBtConnectState.setText("与设备断开连接");
                    mBlthChatUtil.startListen();
                    break;
                case BluetoothChatUtil.MESSAGE_READ: {
                   /* byte[] buf = msg.getData().getByteArray(BluetoothChatUtil.READ_MSG);
                    String str = new String(buf, 0, buf.length);
//				 str=FileUtil.stringToAscii(str);
                    Log.e("YJL", "str.length" + str.length() + "buf.length" + buf.length);
                    char date[] = new char[buf.length*2];
                    FileUtil.AscToHex(date, buf, buf.length);
                    String strDate = new String(date);
                    str = String.format("sendApdu:%s", strDate);
                    Log.e("YJL", "str.length" + str.length() );*/
                    String str = msg.getData().getString(BluetoothChatUtil.READ_MSG);
                    Log.e("YJL", "str.length" + str.length());
                    Toast.makeText(getApplicationContext(), "读成功" + str, Toast.LENGTH_SHORT).show();
                    mTvChat.setText(mTvChat.getText().toString() + "\n" + str);
                    break;
                }
                case BluetoothChatUtil.MESSAGE_WRITE: {
                    byte[] buf = (byte[]) msg.obj;
//                    String str = new String(buf, 0, buf.length);
                    String str = BJCWUtil.HexTostr(buf, buf.length);
                    Toast.makeText(getApplicationContext(), "发送成功" + str, Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };
    private Button start_service;
    private TextView command;
    private StringBuilder stringBuilder;
    private StringBuilder resultBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_bluetooth);
        mContext = this;
        stringBuilder = new StringBuilder();
        resultBuilder = new StringBuilder();
        initView();
        initBluetooth();
        register();
//        mBlthChatUtil = BluetoothChatUtil.getInstance(mContext);
//        mBlthChatUtil.registerHandler(mHandler);
    }

    private void register() {
        Log.e("YJL", "注册方法");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contents.TYPE_BLUE);
        filter.addAction(Contents.CONNECT_FAIL);
        filter.addAction(Contents.CONNECT_SUCCESS);
        filter.addAction(Contents.TYPE_BLE);
        filter.addAction(Contents.COMMAND_CODE);
        filter.addAction(Contents.TYPE_WIFI);
        registerReceiver(receiver, filter);
    }

    //初始化空间
    private void initView() {
        mBtnBluetoothVisibility = (Button) findViewById(R.id.btn_blth_visiblity);
        mBtnBluetoohDisconnect = (Button) findViewById(R.id.btn_blth_disconnect);
        mBtnSendMessage = (Button) findViewById(R.id.btn_sendmessage);
        mEdttMessage = (EditText) findViewById(R.id.edt_message);
        mBtConnectState = (TextView) findViewById(R.id.tv_connect_state);
        mTvChat = (TextView) findViewById(R.id.tv_chat);

        mBtnBluetoothVisibility.setOnClickListener(this);
        mBtnBluetoohDisconnect.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);
        start_service = ((Button) findViewById(R.id.start_service));
        start_service.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BluetoothServerActivity.this, ClassicBluetoothService.class);
                startService(intent1);
                Intent intent2 = new Intent(BluetoothServerActivity.this, BleService.class);
                startService(intent2);
                Intent intent3 = new Intent(BluetoothServerActivity.this, WifiService.class);
                startService(intent3);
            }
        });
        command = ((TextView) findViewById(R.id.command));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (Contents.TYPE_BLUE.equals(action)) {
                //经典蓝牙
                sendMessageBySerialPortOrWifi(intent, true);
            } else if (Contents.CONNECT_FAIL.equals(action)) {
                start_service.setText("服务开启失败，请重新开启");
            } else if (Contents.CONNECT_SUCCESS.equals(action)) {
                start_service.setText("服务已开启");
            } else if (Contents.COMMAND_CODE.equals(action)) {
                String commands = intent.getStringExtra(Contents.KEY_BLE);
                playVoice(commands);
            } else if (Contents.TYPE_BLE.equals(action)) {
                //ble
                String commands = intent.getStringExtra(Contents.KEY_BLE);
                command.setText(commands);
            } else if (Contents.TYPE_WIFI.equals(action)) {
                //wifi
                sendMessageBySerialPortOrWifi(intent, false);
            }
        }
    };
    private String cmd;
    private int code = 10;

    void sendMessageBySerialPortOrWifi(final Intent intent, final boolean blueOrWifi) {
        if (blueOrWifi) {
            //true：blue  false：wifi
            cmd = intent.getStringExtra(Contents.KEY_BLUE);
        } else {
            cmd = intent.getStringExtra(Contents.KEY_WIFI);
           /* if (cmd.startsWith("0")) {
                code = Integer.parseInt(cmd.substring(0, 2));
                Log.e("YJL", "commandCode===" + code);
                cmd = cmd.substring(2);
                playVoice(code);
            } else {
                code = 10;
            }*/
        }
        playVoice(cmd);
        BoxDataUtils.getData(cmd, new BoxDataUtils.DataCallBack() {
            @Override
            public void onSuccess(final String version) {
                command.setText("");
                Log.e("YJL", "version==" + version);
                            /*如果是明文或者密文指令*/
                if ("2A".equals(version)) {
                    stringBuilder.append(new String(BJCWUtil.StrToHex(version)));
                    command.setText(stringBuilder.toString());
                } else if ("08".equals(version)) {
                    if (stringBuilder.toString().length() > 0) {
                        command.setText(stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1));
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(command.getText().toString());
                    } else {
                        stringBuilder = new StringBuilder();
                    }
                } else {
                    if (!cmd.startsWith("F5") && !cmd.startsWith("F0")) {
                        //查看是否明文密文指令
                        command.setText("");
                    }
                    resultBuilder.append(version);
                    stringBuilder = new StringBuilder();
                    String result = resultBuilder.toString();
                    String strSW = resultBuilder.substring(resultBuilder.length() - 4);
                    int pulSW = Integer.valueOf(strSW, 16);
                    Log.e("YJL", "pulSw===" + pulSW);
                    if (pulSW == 0x9000) {
                        boolean completion = BJCWUtil.judgeData(result);
                        if (completion) {
                            if (blueOrWifi) {
                                //true：blue  false：wifi
                                AppConfig.getInstance().getmBluetoothChatUtil().write(BJCWUtil.StrToHex(result));
                            } else {
                                AppConfig.getInstance().getSocketServerUtil().sendMessage(BJCWUtil.StrToHex(result));
                            }
                            resultBuilder = new StringBuilder();
                        } else {
                            resultBuilder = new StringBuilder();
                        }
                    }
                }
            }

            @Override
            public void onFail() {

            }
        });


    }

    private void playVoice(String s) {
        Log.e("YJL", "command===" + s);
        switch (s) {
            //获取版本指令
            case Contents.COMMAND_VERSION:
                break;
            //IC_接触卡指令
            case Contents.COMMAND_IC_CONTACT_1:
                break;
            //IC_非接触卡指令
            case Contents.COMMAND_IC_NOCONTACT_1:
                break;
            //身份证指令
            case Contents.COMMAND_IDCARD_1:
                break;
            //指纹指令
            case Contents.COMMAND_FINGER:
                break;
            //磁条卡指令
            case Contents.COMMAND_MAGNETIC:
                break;
            //明文指令
            case Contents.COMMAND_PIN_PLAINTEXT:
                break;
            //密文指令
            case Contents.COMMAND_PIN_CIPHERTEXT_1:
                break;

        }
    }

    private void playVoice(int commandCode) {
        switch (commandCode) {
            case 0:
                //0：获取版本号
                break;
            case 1:
                //1：接触卡
                break;
            case 2:
                //2：非接触卡
                break;
            case 3:
                //3:读身份证
                break;
            case 4:
                //4:磁条卡
                break;
            case 5:
                //5:键盘输入pin--明文
                break;
            case 6:
                //6:键盘输入pin--密文
                break;
            case 7:
                //7:指纹模块版本
                break;
            case 8:
                //8:指纹模块获取
                break;
            case 9:
                //9:指纹特征获取
                break;
        }
    }

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
//        if (mBluetoothAdapter.isEnabled()) {
//            if (mBluetoothAdapter.getScanMode() !=
//                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                Intent discoverableIntent = new Intent(
//                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(
//                        BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
//                Log.e("YJL", mBluetoothAdapter.getAddress() + mBluetoothAdapter.getName());
//            }
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult request=" + requestCode + " result=" + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //后台修改蓝牙的可见性
                setDiscoverableTimeout(300);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) return;
        //TODO 开启服务
      /*  if (mBlthChatUtil != null) {
            // 只有国家是state_none，我们知道，我们还没有开始
            if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_NONE) {
                // 启动蓝牙聊天服务
                mBlthChatUtil.startListen();
            } else if (mBlthChatUtil.getState() == BluetoothChatUtil.STATE_CONNECTED) {
                BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
                if (null != device && null != device.getName()) {
                    mBtConnectState.setText("已成功连接到设备" + device.getName());
                } else {
                    mBtConnectState.setText("已成功连接到设备");
                }
            }
        }*/
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
        if (null != AppConfig.getInstance().getServer().getServices() || 0 == AppConfig.getInstance().getServer().getServices().size()) {
            AppConfig.getInstance().getServer().clearServices();
        }
        AppConfig.getInstance().getServer().close();
        AppConfig.getInstance().getmBluetoothChatUtil().unregisterHandler();
        AppConfig.getInstance().getmBluetoothChatUtil().disconnect();
        AppConfig.getInstance().getSocketServerUtil().unregisterHandler();
        AppConfig.getInstance().getSocketServerUtil().disconnect();
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btn_blth_visiblity:
                if (mBluetoothAdapter.isEnabled()) {
                    if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoveryIntent = new Intent(
                                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoveryIntent.putExtra(
                                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoveryIntent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.bluetooth_unopened), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_blth_disconnect:
                if (mBlthChatUtil.getState() != BluetoothChatUtil.STATE_CONNECTED) {
                    Toast.makeText(mContext, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                } else {
                    mBlthChatUtil.disconnect();
                }
                break;
            case R.id.btn_sendmessage:
                String messagesend = mEdttMessage.getText().toString();
                if (null == messagesend || messagesend.length() == 0) {
                    return;
                }

//                mBlthChatUtil.write(messagesend.getBytes());
                mBlthChatUtil.write(BJCWUtil.StrToHex("52000A76312E302E302E319000"));
                break;
            default:
                break;
        }
    }

    /*开启的可见性，还有个附件的属性，timeout值并没有起到作用，可见性是一直保持的*/
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

    /*关闭可见性*/
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

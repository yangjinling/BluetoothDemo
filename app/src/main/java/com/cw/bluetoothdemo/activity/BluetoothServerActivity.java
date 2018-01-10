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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.cw.bluetoothdemo.util.Control;
import com.cw.bluetoothdemo.view.DialogListener;
import com.cw.bluetoothdemo.view.WritePadDialog;
import com.wellcom.finger.FpDriverV12;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public static final String sBmpFile = Environment.getExternalStorageDirectory() + "/finger_wl.bmp";
    private FpDriverV12 mIFpDevDriver;
    private String mb;
    private String tz;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String[] arrRet;
            prograss.setVisibility(View.GONE);
            Contents.play_Finger = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearAnimator();
                }
            }, 1000);
            switch (msg.what) {
                case 0:
                    clearAnimator();
                    arrRet = (String[]) msg.obj;
                    lists.add("" + arrRet[0] + arrRet[1]);
                    adapter.notifyDataSetChanged();
                    if (arrRet[0].equals("0")) {
                        dealFingerData("52" + arrRet[1] + "9000");
                        Log.e("YJL", "version==" + arrRet[1]);
                    } else {
//                        tv_version.setText("");
                        Log.e("YJL", "version==失败");
//                        getVersion(blueOrWifi, ble);
                        dealFingerData("52" + arrRet[0] + arrRet[1] + "9000");
                        Toast.makeText(getApplicationContext(), "版本获取失败，重新获取", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    //模板
                    clearAnimator();
                    arrRet = (String[]) msg.obj;
                    lists.add("" + arrRet[0] + arrRet[1]);
                    adapter.notifyDataSetChanged();
                    if (arrRet[0].equals("0")) {
                        mb = arrRet[1];
                        Log.e("YJL", "mb===" + mb);
                        dealFingerData("52" + mb + "9000");
                    } else {
                        mb = "";
//                        getMB(blueOrWifi, ble);
                        dealFingerData("52" + arrRet[0] + arrRet[1] + "9000");
                        Toast.makeText(getApplicationContext(), "模板获取失败，重新获取", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    //特征
                    clearAnimator();
                    arrRet = (String[]) msg.obj;
                    lists.add("" + arrRet[0] + arrRet[1]);
                    adapter.notifyDataSetChanged();
                    if (arrRet[0].equals("0")) {
                        tz = arrRet[1];
                        dealFingerData("52" + tz + "9000");
                        Log.e("YJL", "tz===" + tz);
                    } else {
                        tz = "";
                        dealFingerData("52" + arrRet[0] + arrRet[1] + "9000");
                        Toast.makeText(getApplicationContext(), "特征获取失败，重新获取", Toast.LENGTH_SHORT).show();
//                        getTZ(blueOrWifi, ble);
                    }
                    break;
            }
        }
    };
    private boolean blueOrWifi;
    private boolean ble;
    private Button sign;
    String[] arrRet;
    private ImageView imageview;
    private ImageView contact;
    private ImageView nocontact;
    private ImageView finger;
    private ImageView magnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_bluetooth);
        mContext = this;
        stringBuilder = new StringBuilder();
        resultBuilder = new StringBuilder();
        //初始化控件
        initView();
        //初始化蓝牙
        initBluetooth();
        //注册广播且处理数据
        register();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mIFpDevDriver = new FpDriverV12(getApplicationContext());// finger print driver
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Toast.makeText(BluetoothServerActivity.this, "初始化指纹仪", Toast.LENGTH_SHORT).show();
                initFinger();
            }
        }, 5000);

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
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, lists);
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
        sign = ((Button) findViewById(R.id.sign));
        sign.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BluetoothServerActivity.this, SignActivity.class);
                startActivity(intent);
//                showDialog();
            }
        });
        command = ((TextView) findViewById(R.id.command));
        prograss = ((LinearLayout) findViewById(R.id.layout_prograss));
        prograss_tv = ((TextView) findViewById(R.id.prograss_tv));
        imageview = ((ImageView) findViewById(R.id.imageview));
        contact = ((ImageView) findViewById(R.id.iv_contact));
        nocontact = ((ImageView) findViewById(R.id.iv_nocontact));
        finger = ((ImageView) findViewById(R.id.iv_finger));
        magnetic = ((ImageView) findViewById(R.id.iv_magnetic));
    }

    private void dealFingerData(String data) {
        if (blueOrWifi) {
            if (ble) {
                dealDate(data.getBytes(), mDevice, AppConfig.getInstance().getCharacter(), AppConfig.getInstance().getServer());
            } else {
                AppConfig.getInstance().getmBluetoothChatUtil().write(data.getBytes());
            }
        } else {
            AppConfig.getInstance().getSocketServerUtil().sendMessage(data.getBytes());

        }

    }

    private void dealFingerData(byte[] data) {
        Log.e("YJL", "data==" + data + "---" + data.length);
        if (blueOrWifi) {
            if (ble) {
                dealDate(data, mDevice, AppConfig.getInstance().getCharacter(), AppConfig.getInstance().getServer());
            } else {
                AppConfig.getInstance().getmBluetoothChatUtil().write(data);
            }
        } else {
            AppConfig.getInstance().getSocketServerUtil().sendMessage(data);

        }

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
                blueOrWifi = true;
                ble = false;
            } else if (Contents.TYPE_BLE.equals(action)) {
                //ble蓝牙
                blueOrWifi = true;
                ble = true;
                dealMessage(intent, true, true);
            } else if (Contents.TYPE_WIFI.equals(action)) {
                //wifi
                ble = false;
                blueOrWifi = false;
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
        if (Contents.COMMAND_FINGER_VERSION.equals(cmd)) {
            //指纹usb---获取版本
            if (g_bIsRunning) {
                Log.e("YJL", "设备忙");
                return;
            }
            nocontact.setVisibility(View.GONE);
            contact.setVisibility(View.GONE);
            finger.setVisibility(View.VISIBLE);
            finger.setBackgroundResource(R.mipmap.finger);
            displayWholeAnimation(2);//指纹
            getVersion(blueOrWifi, ble);
        } else if (Contents.COMMAND_FINGER_FEATURE.equals(cmd)) {
            //指纹usb---获取特征
            if (g_bIsRunning) {
                Log.e("YJL", "设备忙");
                return;
            }
            nocontact.setVisibility(View.GONE);
            contact.setVisibility(View.GONE);
            finger.setVisibility(View.VISIBLE);
            finger.setBackgroundResource(R.mipmap.finger);
            displayWholeAnimation(2);//指纹
            getTZ(blueOrWifi, ble);
        } else if (Contents.COMMAND_FINGER_MODE.equals(cmd)) {
            //指纹usb---获取模板
            if (g_bIsRunning) {
                Log.e("YJL", "设备忙");
                return;
            }
            nocontact.setVisibility(View.GONE);
            contact.setVisibility(View.GONE);
            finger.setVisibility(View.VISIBLE);
            finger.setBackgroundResource(R.mipmap.finger);
            displayWholeAnimation(2);//指纹
            getMB(blueOrWifi, ble);
        } else if (Contents.COMMAND_SIGN.equals(cmd)) {
            //签名
            Intent intent1 = new Intent(BluetoothServerActivity.this, SignActivity.class);
            startActivityForResult(intent1, 100);
//            showDialog();
        } else {
            //串口发送指令并处理
            sendMessagBySerialPort(cmd, blueOrWifi, ble);
        }
    }

    /*指纹设备初始化*/
    public void initFinger() {
        mIFpDevDriver.cancel();
        mIFpDevDriver.closeDevice();
        arrRet = mIFpDevDriver.openDevice();
        lists.add("" + arrRet[0] + arrRet[1]);
        adapter.notifyDataSetChanged();
    }

    /**
     * 指纹获取版本
     */
    private void getVersion(boolean blueOrWifi, boolean ble) {
        initFinger();
        this.blueOrWifi = blueOrWifi;
        this.ble = ble;
       /* new Thread() {
            public void run() {*/
        String[] arrRet;
        g_bIsRunning = true;
        arrRet = mIFpDevDriver.getFpVersion();
        g_bIsRunning = false;
        Message message = new Message();
        message.what = 0;
        message.obj = arrRet;
        handler.sendMessageDelayed(message, 10);
            /*    return;
            }
        }.start();*/
    }

    /**
     * 指纹获取特征--1次
     */
    boolean g_bIsRunning = false;

    private void getTZ(boolean blueOrWifi, boolean ble) {
        initFinger();
        this.blueOrWifi = blueOrWifi;
        this.ble = ble;
        new Thread() {
            public void run() {
                String[] arrRet;
                g_bIsRunning = true;
                arrRet = mIFpDevDriver.readFinger(10);
                g_bIsRunning = false;
                Message message = new Message();
                message.what = 2;
                message.obj = arrRet;
                handler.sendMessageDelayed(message, 10);
                return;
            }
        }.start();
    }

    /**
     * 指纹获取模板---3次
     */
    private void getMB(boolean blueOrWifi, final boolean ble) {
        initFinger();
        this.blueOrWifi = blueOrWifi;
        this.ble = ble;
        new Thread() {
            public void run() {
                String[] arrRet;
                g_bIsRunning = true;
                arrRet = mIFpDevDriver.registerFinger(40);
                g_bIsRunning = false;
                Message message = new Message();
                message.what = 1;
                message.obj = arrRet;
                handler.sendMessageDelayed(message, 10);
                return;
            }
        }.start();
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
//                        command.setText("");
                    }
                    if (data.equals("00026984") || data.equals("5200026984")) {
                        sendMessagBySerialPort(cmd, blueOrWifi, ble);
                    } else {
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
//                                command.setText("" + strSW);
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
//                            resultBuilder = new StringBuilder();
//                            sendMessagBySerialPort(cmd, blueOrWifi, ble);
                            }
                        } else {
                            //数据长度不正确重新发送
//                        resultBuilder = new StringBuilder();
//                        //重发
//                        sendMessagBySerialPort(cmd, blueOrWifi, ble);
                        }
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
                command.setVisibility(View.GONE);
                command.setText("");
                prograss.setVisibility(View.VISIBLE);
                prograss_tv.setText("获取固件版本");
                break;
            //IC_接触卡指令
            case Contents.COMMAND_IC_CONTACT_1:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Contact) {
//                    prograss.setVisibility(View.VISIBLE);
//                    prograss_tv.setText("请插卡");
                    setPlayVoice(1);
                    nocontact.setVisibility(View.GONE);
                    contact.setVisibility(View.VISIBLE);
                    finger.setVisibility(View.GONE);
                    magnetic.setVisibility(View.GONE);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_BANKCARD1, null);
                    contact.setBackgroundResource(R.mipmap.card);
                    displayWholeAnimation(1);//接触
                }
                break;
            //IC_非接触卡指令
            case Contents.COMMAND_IC_NOCONTACT_1:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_NoContact) {
                    nocontact.setVisibility(View.VISIBLE);
                    contact.setVisibility(View.GONE);
                    finger.setVisibility(View.GONE);
                    magnetic.setVisibility(View.GONE);
//                    prograss.setVisibility(View.VISIBLE);
//                    prograss_tv.setText("请挥卡");
                    nocontact.setBackgroundResource(R.mipmap.card);
                    setPlayVoice(2);
                    displayWholeAnimation(0); //非接触
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_BANKCARD2, null);
                }
                break;
            //身份证指令
            case Contents.COMMAND_IDCARD_1:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Idcard) {
//                    prograss.setVisibility(View.VISIBLE);
//                    prograss_tv.setText("请放身份证");
                    nocontact.setVisibility(View.VISIBLE);
                    contact.setVisibility(View.GONE);
                    finger.setVisibility(View.GONE);
                    magnetic.setVisibility(View.GONE);
                    setPlayVoice(3);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_IDCARD, null);
                    nocontact.setBackgroundResource(R.mipmap.idcard);
                    displayWholeAnimation(0);//接触
                }
                break;
            //磁条卡指令
            case Contents.COMMAND_MAGNETIC:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Magnetic) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请刷磁条卡");
                    setPlayVoice(4);
                    nocontact.setVisibility(View.GONE);
                    contact.setVisibility(View.GONE);
                    finger.setVisibility(View.GONE);
                    magnetic.setVisibility(View.VISIBLE);
                    displayWholeAnimation(3);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_MAGNETIC, null);
                }
                break;
            //明文指令
            case Contents.COMMAND_PIN_PLAINTEXT:
                command.setVisibility(View.VISIBLE);
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
                command.setVisibility(View.VISIBLE);
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
            case Contents.COMMAND_FINGER_VERSION:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Finger) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按指纹");
                    setPlayVoice(7);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_FINGER, null);
                }
                break;
            case Contents.COMMAND_FINGER_FEATURE:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Finger) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按指纹");
                    setPlayVoice(7);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_FINGER, null);
                }
                break;
            case Contents.COMMAND_FINGER_MODE:
                command.setVisibility(View.GONE);
                command.setText("");
                if (!Contents.play_Finger) {
                    prograss.setVisibility(View.VISIBLE);
                    prograss_tv.setText("请按指纹");
                    setPlayVoice(7);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_FINGER, null);
                }
                break;
            //签名
            case Contents.COMMAND_SIGN:
                command.setText("");
                if (!Contents.play_sign) {
//                    prograss.setVisibility(View.VISIBLE);
//                    prograss_tv.setText("请签名");
                    setPlayVoice(7);
                    MediaServiceManager.startService(BluetoothServerActivity.this, Contents.VOICE_SIGN, null);
                }
                break;
            //执行刷卡结束的指令后播放标记置为false
            case Contents.COMMAND_IC_END:
                setPlayVoice(8);
                clearAnimator();
                break;
            //执行身份证结束的指令后播放标记置为false
            case Contents.COMMAND_IDCARD_2:
                setPlayVoice(8);
                clearAnimator();
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
            if (Contents.COMMAND_CURRENT.equals(Contents.COMMAND_IC_END)
                    || Contents.COMMAND_CURRENT.equals(Contents.COMMAND_IDCARD_2)) {
                clearAnimator();
            }
            //身份证最后一条指令/刷卡最后一条指令/获取版本
            prograss.setVisibility(View.GONE);

        } else {
            if (Contents.play_Magnetic) {
                prograss.setVisibility(View.GONE);
                clearAnimator();
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
        Contents.play_sign = false;
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
        } else if (requestCode == 100 && resultCode == RESULT_OK) {

//            byte[] bytes = Control.getBytes(Contents.folderStr);
            byte[] bytes = Control.getBitmapByte(AppConfig.mBitmap);
            send(bytes, "photo");
            AppConfig.mBitmap = null;
            Contents.play_sign = false;
            Log.e("YJL", "bytes===" + bytes.length);
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
        mIFpDevDriver.cancel();
        mIFpDevDriver.closeDevice();
        Control.gpio_control(927, 0);//串口
        Control.gpio_control(921, 0);//host3
        Control.gpio_control(920, 0);//host4
        Control.gpio_control(922, 0);//host1
        Control.gpio_control(1006, 0);//host2
        Control.gpio_control(1010, 0);//hub
        Control.gpio_control(1009, 0);//hub
        Control.gpio_control(969, 0);//切换host
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

    private Bitmap mSignBitmap;
    private String signPath;

    private void showDialog() {
        WritePadDialog writeTabletDialog = new WritePadDialog(
                BluetoothServerActivity.this, R.style.SignBoardDialog, new DialogListener() {
            public void refreshActivity(Object object) {
                mSignBitmap = (Bitmap) object;
//                signPath = createFile();

                //对图片进行压缩
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 15;
//                options.inTempStorage = new byte[5 * 1024];
//                Bitmap zoombm = BitmapFactory.decodeFile(signPath, options);

//                Bitmap zoombm = getCompressBitmap(signPath);
                //ivSign.setImageBitmap(mSignBitmap);
                byte[] bytes = Control.getBitmapByte(mSignBitmap);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageview.setImageBitmap(bmp);
                send(bytes, "photo");

            }
        });
        writeTabletDialog.show();
    }

    /**
     * 创建手写签名文件
     *
     * @return
     */
    private String createFile() {
        ByteArrayOutputStream baos = null;
        String _path = null;
        try {
            String sign_dir = Contents.folderStr;
            _path = sign_dir + System.currentTimeMillis() + ".png";
            baos = new ByteArrayOutputStream();
            mSignBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] photoBytes = baos.toByteArray();
            if (photoBytes != null) {
                new FileOutputStream(new File(_path)).write(photoBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _path;
    }

    /**
     * 根据图片路径获取图片的压缩图
     *
     * @param filePath
     * @return
     */
    public Bitmap getCompressBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options); //此时返回bm为空
        if (bitmap == null) {
        }
        //计算缩放比
        int simpleSize = (int) (options.outHeight / (float) 200);
        if (simpleSize <= 0)
            simpleSize = 1;
        options.inSampleSize = simpleSize;
        options.inJustDecodeBounds = false;
        //重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
        bitmap = BitmapFactory.decodeFile(filePath, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        System.out.println(w + "   " + h);
        return bitmap;
    }

    //添加头发送数据
    public void send(byte[] data, String str) {
        int length = data.length;
        Log.e("YJL", "length--->>" + length);
        byte[] length_b = null;
        try {
            length_b = Control.intToByteArray(length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (length_b == null) return;

        //获得一个字节长度为14的byte数组 headInfoLength为14
        byte[] headerInfo = new byte[14];

        //前六位添加012345的标志位
        for (int i = 0; i < 6; i++) {
            headerInfo[i] = (byte) i;
        }

        //7到10位添加图片大小的字节长度
        for (int i = 0; i < 4; i++) {
            headerInfo[6 + i] = length_b[i];
        }

        //11到14位添加动作信息
        if (str.equals("photo")) {
            for (int i = 0; i < 4; i++) {
                headerInfo[10 + i] = (byte) 1;
            }
        }
        //将对应信息添加到图片前面
        byte[] sendMsg = new byte[length + 14];
        for (int i = 0; i < sendMsg.length; i++) {
            if (i < 14) {
                sendMsg[i] = headerInfo[i];
            } else {
                sendMsg[i] = data[i - 14];
            }
        }
        dealFingerData(sendMsg);
    }


    private void startAppearanceAnimation(int type) {
        /**
         * 核心类 AnimationSet 顾名思义，可以简单理解为将多种动画放在一个set集合里面
         *    产生渐渐显示+位移动画，将加速小火箭渐渐显示出来;
         *
         */
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        TranslateAnimation translateAnimation = null;
        if (type == 0) {
            translateAnimation = new TranslateAnimation(0, 200, 0, 0);
        } else if (type == 1) {
            translateAnimation = new TranslateAnimation(0, 0, 0, -200);
        } else if (type == 2) {
            translateAnimation = new TranslateAnimation(0, -500, 0, 0);
        } else if (type == 3) {
            translateAnimation = new TranslateAnimation(0, 0, 0, 500);
        }
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        alphaAnimation.setRepeatCount(Integer.MAX_VALUE);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillAfter(true);
        animationSet.setDuration(2000);
        animationSet.setRepeatMode(Integer.MAX_VALUE);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        if (type == 0) {
            nocontact.startAnimation(animationSet);
        } else if (type == 1) {
            contact.startAnimation(animationSet);
        } else if (type == 2) {
            finger.startAnimation(animationSet);
        } else if (type == 3) {
            magnetic.startAnimation(animationSet);
        }
    }

    private void startDisappearanceAnimation(int type) {
        TranslateAnimation translateAnimation = null;
        if (type == 0) {
            translateAnimation = new TranslateAnimation(0, 200, 0, 0);
        } else if (type == 1) {
            translateAnimation = new TranslateAnimation(0, 0, 0, -200);
        } else if (type == 2) {
            translateAnimation = new TranslateAnimation(0, -150, 0, 0);
        } else if (type == 3) {
            translateAnimation = new TranslateAnimation(0, 0, 0, 500);
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setFillAfter(true);
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        alphaAnimation.setRepeatCount(Integer.MAX_VALUE);
        animationSet.setDuration(2000);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        if (type == 0) {
            nocontact.startAnimation(animationSet);
        } else if (type == 1) {
            contact.startAnimation(animationSet);
        } else if (type == 2) {
            finger.startAnimation(animationSet);
        } else if (type == 3) {
            magnetic.startAnimation(animationSet);
        }
    }

    private void displayWholeAnimation(final int type) {
        startAppearanceAnimation(type);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startDisappearanceAnimation(type);
            }
        }, 100);
    }

    private void clearAnimator() {
        Log.e("YJL", "取消动画");
        nocontact.clearAnimation();
        contact.clearAnimation();
        finger.clearAnimation();
        magnetic.clearAnimation();
        magnetic.invalidate();
        nocontact.invalidate();
        finger.invalidate();
        contact.invalidate();
        nocontact.setVisibility(View.GONE);
        contact.setVisibility(View.GONE);
        finger.setVisibility(View.GONE);
        magnetic.setVisibility(View.GONE);

    }
}

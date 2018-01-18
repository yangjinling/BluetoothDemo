package com.cw.bluetoothdemo.app;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

/**
 * Created by yangjinling on 2017/11/28.
 */

public class Contents {
    public final static String TYPE_BLUE = "READ_BLUE";//蓝牙接收到客户端发来的指令
    public final static String TYPE_WIFI = "READ_WIFI";//WIFI接收到客户端发来的指令
    public final static String KEY_BLUE = "DATA_BLUE";//蓝牙接收到客户端发来指令的键值
    public final static String KEY_WIFI = "DATA_WIFI";//WIFI接收到客户端发来指令的键值
    public final static String TYPE_BLE = "READ_BLE";
    public final static String KEY_BLE = "DATA_BLE";//
    public final static String KEY_DEVICE = "DATA_DEVICE";//
    public final static String COMMAND_CODE = "COMMAND_CODE";
    public final static String CONNECT_SUCCESS = "SUCCESS";//服务开启成功
    public final static String CONNECT_FAIL = "FAIL";//服务开启失败
    public final static String serviceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    public final static String characteristicUUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    //获取版本指令
    public final static String COMMAND_VERSION = "F80007FB812000000000";
    //IC_接触卡指令
    public final static String COMMAND_IC_CONTACT_1 = "EE0011FB80201400000A62000000000000000000";
    //IC_非接触卡指令
    public final static String COMMAND_IC_NOCONTACT_1 = "EE0011FB80101400000A62000000000000000000";

    public final static String COMMAND_IC_END = "F80007FB810005000000";
    //身份证指令
    public final static String COMMAND_IDCARD_1 = "F80007FB801114000000";
    public final static String COMMAND_IDCARD_2 = "F80007FB810005000000";
    //指纹指令
//    public final static String COMMAND_FINGER = "F80007FB803414000000";
    //磁条卡指令
    public final static String COMMAND_MAGNETIC = "F80007FB800014000000";
    //明文指令
    public final static String COMMAND_PIN_PLAINTEXT = "F5000AFB802314000003000001";
    //密文指令第一条
    public final static String COMMAND_PIN_CIPHERTEXT_1 = "F0000FFB8025140000080000000000000000";
    //指纹获取版本
    public final static String COMMAND_FINGER_VERSION = "08";
    //指纹获取特征
    public final static String COMMAND_FINGER_FEATURE = "09";
    //指纹获取模板
    public final static String COMMAND_FINGER_MODE = "10";
    //签名指令
    public final static String COMMAND_SIGN = "11";
    //当前接收到手机端的指令
    public static String COMMAND_CURRENT = "";

    //插卡语音
    public final static String VOICE_BANKCARD1 = "contact";//请插卡
    //挥卡语音
    public final static String VOICE_BANKCARD2 = "nocontact";//请挥卡
    //插卡语音
    public final static String VOICE_MAGNETIC = "magnetic";//请刷磁条卡
    //身份证语音
    public final static String VOICE_IDCARD = "idcard";//请放身份证
    //输入密码
    public final static String VOICE_KEY = "key";//请按键
    //指纹
    public final static String VOICE_FINGER = "finger";//请按指纹
    //签名
    public final static String VOICE_SIGN = "sign";//请签名

    public final static  String COMMAND_CANCLE="FA";
    public static boolean play_Contact = false;
    public static boolean play_NoContact = false;
    public static boolean play_Idcard = false;
    public static boolean play_Magnetic = false;
    public static boolean play_KeyMing = false;
    public static boolean play_KeyMi = false;
    public static boolean play_Finger = false;
    public static boolean isControl = false;
    public static boolean play_sign = false;
    public static String folderStr = Environment.getExternalStorageDirectory().getPath() + "/Bluetooth/Sign/";
    public static String path="";
    public static File file;
    public static Activity activity;
}

package com.cw.bluetoothdemo.app;

/**
 * Created by yangjinling on 2017/11/28.
 */

public class Contents {
    public final static String TYPE_BLUE = "READ_BLUE";//蓝牙接收到客户端发来的指令
    public final static String TYPE_WIFI = "READ_WIFI";//WIFI接收到客户端发来的指令
    public final static String KEY_BLUE = "DATA_BLUE";//蓝牙接收到客户端发来指令的键值
    public final static String KEY_WIFI = "DATA_WIFI";//WIFI接收到客户端发来指令的键值
    public final static String TYPE_BLE = "TYPE_BLE";
    public final static String KEY_BLE = "DATA_BLE";//
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
    //身份证指令
    public final static String COMMAND_IDCARD_1 = "F80007FB801114000000";
    //指纹指令
    public final static String COMMAND_FINGER = "F80007FB803414000000";
    //磁条卡指令
    public final static String COMMAND_MAGNETIC = "F80007FB800014000000";
    //明文指令
    public final static String COMMAND_PIN_PLAINTEXT = "F5000AFB802314000003000001";
    //密文指令
    public final static String COMMAND_PIN_CIPHERTEXT_1 = "F0000FFB8025140000080000000000000000";

    //当前接收到手机端的指令
    public static String COMMAND_CURRENT = "";
}
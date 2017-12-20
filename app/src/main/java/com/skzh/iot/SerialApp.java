package com.skzh.iot;

import java.io.File;
import java.io.IOException;

import android.content.Context;//内容的上下文 背景，环境语境什么的
import android.content.SharedPreferences;

import com.cw.bluetoothdemo.app.AppConfig;

public class SerialApp {
	private SerialPort mSerialPort = null;
	private static final String PREFS_NAME = "SerialPort"; // 参数名称
	private static String device = "ttyHSL0"; // 设备名称
	private int baudrate = 115200; // 波特率
	// private static String device = "ttyS3"; // 设备名称

	public SerialPort getSerialPort(Context context)
			throws SecurityException, IOException {
		String path = "/dev/";
		if (mSerialPort == null) {
			/* Open the serial port */
			SharedPreferences sp = context.getSharedPreferences(
					SerialApp.PREFS_NAME, Context.MODE_PRIVATE);//
			// 语境获得共享偏好设置串行程序名称上下文私有模式
			// SharedPreferences数据存储方式获取对象
			// String device = "s3c2410_serial3";//
			// A8:s3c2410_serial0,S3C6410:s3c_serial0,s3c2410_serial3
			// String device = "s3c2410_serial1";

			/*if (sp.contains("device")) {// 如果sp包含设备
				device = sp.getString("device", device);// 获取设备的字符串
				sp.edit().putString("device", device).commit();
			} else {
				sp.edit().putString("device", device).commit();
			}*/
			path = path + device;
			// 新建一个串行端口，在串行端口里面新建文件的路径，波特率，方向）
			mSerialPort = new SerialPort(new File(path), baudrate, 0);

			AppConfig.getInstance().setmSerialPort(mSerialPort);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

}

/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.skzh.iot;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class SerialPort {

	private static final String TAG = "SerialPort";// 字符串标签=“串口”//私有静态

	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(File device, int baudrate, int flags)
			throws SecurityException, IOException {// 旗帜

		if (!device.canRead() || !device.canWrite()) // 如果设备可读可以写
		{
			Log.e("SerialPort", "123"); // 系统/斌/苏
			try {
				Log.e("YJL----->>>", " SSSSS");
				/* Missing read/write permission, trying to chmod the file */// 丢失读写权限，试图chmod文件
				Process su;// 过程
//				Log.e("SerialPort", "su----->>>:"
//						+ Runtime.getRuntime().exec("/system/xbin/su"));
				su = Runtime.getRuntime().exec("/system/xbin/su");// 运行时得到运行时 执行
//				Log.e("SerialPort", "su:" + su); // 系统/斌/苏
				Log.e("SerialPort", "path:"
						+ device.getAbsolutePath());
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";// 字符串命令=“chmod 666”+设备获取绝对路径（）+“\n”+“退出\n”
				// 文件/目录权限设置命令：chmod
				Log.e("SerialPort", "cmd:" + cmd);

				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();// 抛出新的安全异常
				}
			} catch (Exception e) {
				Log.e("SerialPort", "SecurityException");
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e("SerialPort", "native open returns null");// 日志标签 本地打开返回null
			throw new IOException();
		}
		Log.e(TAG, "___打开串口成功____" + mFd);

		mFileInputStream = new FileInputStream(mFd);
		// Log.e("TAG", "mFileInputStream" + mFileInputStream);
		mFileOutputStream = new FileOutputStream(mFd);
		// Log.e("TAG", "mFileOutputStream" + mFileOutputStream);
	}

	// Getters and setters//获取和设置
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate,
			int flags);// 私人本地静态文件描述符的开弦的路径，波特率，int的旗帜

	public native void close();

	static {
		System.loadLibrary("serial_port");// 加载库
	}
}

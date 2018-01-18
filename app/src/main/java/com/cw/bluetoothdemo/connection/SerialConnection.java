package com.cw.bluetoothdemo.connection;

import android.content.Context;

import com.skzh.iot.SerialApp;
import com.skzh.iot.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author yangjinling
 * 
 */
public class SerialConnection {

	private InputStream inputStream;
	private OutputStream outputStream;
	private SerialApp mSerialApp;
	private SerialPort mSerialPort;
	private Context context;

	public SerialConnection(Context context) {
		this.context = context;
		initSerialPort();
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	/**
	 *
	 */
	private void initSerialPort() {
		try {
			mSerialApp = new SerialApp();
			mSerialPort = mSerialApp.getSerialPort(context);
			this.inputStream = mSerialPort.getInputStream();
			this.outputStream = mSerialPort.getOutputStream();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

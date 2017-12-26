/****************************************************************
 * Copyright(C),2012-2015, 浙江维尔科技股份有限公司
 * File name:
 * Author: kf		Version: 1.0.0.1		Date: 2012-03-08
 * Description: 指纹设备接口(JNI)
 * History:
 * 1. Date:
 *    Author:
 *    Modification:  
****************************************************************/

package com.wellcom.finger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import static com.cw.bluetoothdemo.activity.BluetoothServerActivity.sBmpFile;

public class FpDriverV12{
	
	private static final String TAG = "FpDriverV12";
	private int fd = 0;
	private Context context;
	private UsbManager mManager;
	private UsbDeviceConnection mDeviceConnection;

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	public Handler handler;
	/**
	  * 动态库是否已加载标识
	  */
	private static int isLoadOK;
	private static boolean isInit;
	private static int isRoot;
	private static String sLoadErrMsg;
	private static final int IMAGE_LEN_BASE64 = ((152 * 200 + 1078)*4/3+3);
	

	/*
	 * RS232 接口部分
	 */
	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	private String devNode;
	
	/**
	  * 本地方法声明
	  */
	// 设备库
    public native String[] FPIGetDllVersion();
    public native String   FPIGetErrorInfo (int nErrorCode);
    public native int  FPIPowerOn();
    public native int  FPIPowerOff();
    public native int  FPIDeviceInitRS232(byte[] psComFile, int baudrate, int flags);
	public native int  FPIDeviceInit(int fd, String devNode);
	public native int  FPIDeviceClose();
	public native int  FPIGetVersion(byte[] psDevVersion);	
	public native int  FPIGetDeviceID (byte[] psFactoryID);
	public native int  FPIGetFeature (int nTimeOut, byte[] psFeatureBuf, int[] pnLength);
	public native int  FPIGetTemplate (int nTimeOut, byte[] psTemplateBuf, int[] pnLength);
	public native int  FPIGetImage(int nTimeout, byte[] psImageBuf, int[] pnLength, byte[] psBmpFile);
	public native int  FPIGetImageEx(int nTimeout, byte[] psImageBuf, int[] pnLength, int[] iIsValid, int[] piQuality, byte[] psBmpFile);	
	public native int  FPICheckFinger();	
	public native int  FPICheckImage (byte[] psImage, int[] pnErrType, byte[] psErrMsg, byte[] psSuggest);
	public native void FPICancel();
	
	public native int  FPIGatherEnroll(int cGatherNO, int nTimeOut);
	public native int  FPISynEnroll(int nGatherNum);
	public native int  FPIGetFeatureAndImage(int nTimeOut, byte[] psFpData, int[] pnFpDataLen, byte[]  psImageBuf, int[] pnImasgeLen, byte[] psBmpFile);

	// 2017.07.28 新增图像分部采集
	public native int  FPIBeginCapture(int nTimeout);
	public native int  FPIGetFPBmpData(byte[] psImageBuf, int[] pnLength, byte[] psBmpFile);



	// 算法库 
	public native int  FPICryptBase64(int nMode, byte[] psInput, int nInLen, byte[] psOutput, int[] pnOutlen);
	public native int  FPIFpVerify(byte[] psRegBuf, byte[] psVerBuf, int nLevel);
	public native int  FPIGetFeatureByImg(byte[] psImage,  byte[] psVerBuf, int[] pnLength);
	public native int  FPIGetTemplateByImg(byte[] psImage1, byte[] psImage2, byte[] psImage3, byte[] psRegBuf, int[] pnLength);
	public native int  FPIFpMatch(byte[] mb, byte[] tz, int level); 	
	public native int  FPIEnrollX(byte[] psTZ1, byte[] psTZ2, byte[] psTZ3, byte[] psRegBuf, int[] pnLength);
	public native int  FPISaveBMP(String file, byte[] imgBuf);
	

	public FpDriverV12(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		isRoot = 0;
		sLoadErrMsg = "";
		isInit = false;
	}

	public FpDriverV12(Context context) {
		this.context = context;
		isRoot = 0;
		sLoadErrMsg = "";
		isInit = false;
	}

	static
	{
	 	//System.out.println(System.getenv("classpath"));
	 
	 	//BasicConfigurator.configure();
	 	//PropertyConfigurator.configure("log4j.properties");
	 	//logger.BasicConfigurator()
	 	// System.out.println("user.dir="+System.getProperty("user.dir"));
	 	// System.out.println("java.library.path="+System.getProperty("java.library.path"));
		String strLibPath="";
		 try{
			 
			 //IFpDevDriver.logger.debug("====debug");
	
			 isLoadOK = 0;
			 // 方式一：从Java环境路径中加载库文件(使用System.loadLibrary从相对路径加载)	
			 //System.load("/data/com.example.fingerdemo/lib/libfpdriver.so");
			 //System.loadLibrary("devapi");
			 System.loadLibrary("FpDriverV12_CCB");
			 
			 
			 /*
			 // 方法二：从项目中或其它路径中加载库文件(使用System.load从绝对路径加载)
			 if(isWindowsOS()){
				  strLibPath = fullProjectPath("YGQD_WELLCOM_FINGER.dll");
				  strLibPath = strLibPath.substring(1);
				  System.out.println("load library {" + strLibPath + "} OK:");
				  
				  System.load(strLibPath);
			  }else{
				  strLibPath = fullProjectPath("YGQD_WELLCOM_FINGER.so");
		
				  System.out.println("load library {" + strLibPath + "} OK:");
				  System.load(strLibPath);
				  
			  } 
			  */
		 }
		 catch(Throwable  ex){
			 isLoadOK = 1; 
			 sLoadErrMsg = ex.toString();
			 Log.e(TAG, "load library {" + strLibPath + "} fail");
			 Log.e(TAG, sLoadErrMsg);				
		 }
	}
	
	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}
	

 	// 查找指纹设备
 	public int FindAndOpenFpDev() {
 		
 		/*
 		Process process = null;
		 DataOutputStream os = null;
		 try
		 {
			 if (isRoot == 0)
			 {
			   process = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(process.getOutputStream());
				os.writeBytes("chmod 666 /dev/block/sda" + "\n");
				os.writeBytes("exit\n");
			    os.flush();
			    isRoot=1;
			 }
			    
		} 
		catch (Exception e)
		{
			
		} 
		 
		return 0;
		*/

		Log.i(TAG, "FindAndOpenFpDev");
		this.mManager = ((UsbManager) context
				.getSystemService(context.USB_SERVICE));
		if (this.mManager == null) {
			Log.i(TAG, "--->getSystemService failed ");
			Log.i(TAG, "FindAndOpenFpDev End");	
			// Message msg = handler.obtainMessage();
			// msg.what = 1;
			// msg.obj = "getSystemService failed";
			// handler.sendMessage(msg);
			return -1;
		}
		
		Log.i(TAG, "--->getSystemService OK");
		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		if (deviceList != null && deviceList.size() != 0) {
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
					context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
			context.registerReceiver(this.mUsbReceiver, filter);
			while (deviceIterator.hasNext()) {
				UsbDevice device = deviceIterator.next();
				Log.i(TAG, "--->device: VID = " + device.getVendorId() + "PID = " + device.getProductId());
				if((device.getVendorId()==0x2796&&device.getProductId()==0x0998))				
				{
					Log.i(TAG, "--->get fpdev ok");
					if (!mManager.hasPermission(device)) {
						mManager.requestPermission(device, mPermissionIntent);
						Log.i(TAG, "--->mManager.requestPermission");
					}
					
					devNode=device.getDeviceName();
					mDeviceConnection = mManager.openDevice(device);
					if (mDeviceConnection != null) {
						fd = mDeviceConnection.getFileDescriptor();
						Log.i(TAG, "--->getFileDescriptor:" + String.valueOf(fd) + ", devNode = " + devNode);
						Log.i(TAG, "FindAndOpenFpDev End");		
						return 0;
					}
				}
				
			}
		} else {
			Log.i(TAG, "--->Not Find Finger Device");
			Log.i(TAG, "FindAndOpenFpDev End");	
			return -1;
		}
		
		Log.i(TAG, "FindAndOpenFpDev End");		
		return -1;

	}
 	
 	/*
	 * 关闭设备
	 */
	private void FPICloseFpDev() {
		Log.i(TAG, "FPICloseFpDev");
		if (mDeviceConnection != null) {
			mDeviceConnection.close();
			mDeviceConnection = null;
		}

	}

	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "BroadcastReceiver");
			Log.i(TAG, "--->mUsbReceiver" + fd);
			Log.i(TAG, "--->mUsbReceiver" + intent.getAction());
			if (intent.getAction().equals(ACTION_USB_PERMISSION) || intent.getAction().equals("USB_DEVICE_ATTACHED")) {
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					mDeviceConnection = mManager.openDevice(dev);
					
					if (mDeviceConnection != null) {
						fd = mDeviceConnection.getFileDescriptor();
						Log.i(TAG, "mUsbReceiver"+fd);
					}
					Log.i(TAG, "mUsbReceiver");
				}
			}
		}
	};
	
	
	/**
	  * 打开串口电源
	  * @return
	  */	
	 public String[] power_on(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 
			 iRet = FPIPowerOn();				 
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = FPIGetErrorInfo(iRet);
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
	 }
	 
	 /**
	  * 打关闭串口电源
	  * @return
	  */	
	 public String[] power_off(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 
			 iRet = FPIPowerOff();				 
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = FPIGetErrorInfo(iRet);
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
	 }
	
	
	/**
	  * @return
	  */	
	public String[] openDeviceRS232(String path_utf, int baudrate, int flags){
					 
		if (isLoadOK  != 0)
		{
			String[] arrRet = new String[2]; 
			arrRet[0] = "-101";
			arrRet[1] = sLoadErrMsg;
			
			return arrRet;
		}
		
		try{
			
			Log.i(TAG, "openDeviceRS232");
			
			File device = new File(path_utf);
			/* Check access permission */
			if (!device.canRead() || !device.canWrite()) {
				try {
					
					/* Missing read/write permission, trying to chmod the file */
					Process su;
					su = Runtime.getRuntime().exec("/system/bin/su");
					String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
					su.getOutputStream().write(cmd.getBytes());
					if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
						
						throw new SecurityException();
					}

					
				} catch (Exception e) {
					
					e.printStackTrace();
					throw new SecurityException();
				}  
			}
			
			int  iRet = -1;
			iRet = FPIDeviceInitRS232(device.getAbsolutePath().getBytes(), baudrate, flags);
			Log.i(TAG, "FPIDeviceInit = " + iRet);
			String[] arrRet = new String[2]; 
			arrRet[0] = Integer.toString(iRet);
			arrRet[1] = FPIGetErrorInfo(iRet);
			return arrRet; 
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
	 }
	 
	
	/**
	  * 打开指纹设备
	  * @return
	  */	
	 public String[] openDevice(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int iret = -1;
			 // 先关闭原有打开的资源
			 if (isInit){
				 iret = FPIDeviceClose();
				 FPICloseFpDev();
			 }

			// 查收资源句柄
			 iret = FindAndOpenFpDev();
			 if (iret != 0)
				 Log.e(TAG, "FindUSBdevice {" + fd + "} fail");
			 else
				 Log.i(TAG, "FindUSBdevice {" + fd + "} ok");
			 
			 if (iret == 0) {
				 int  iRet = -1;
				 iRet = FPIDeviceInit(fd, devNode);	
				 Log.i(TAG, "FPIDeviceInit = " + iRet + ", devNode = " + devNode);

				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 return arrRet;
				 
			 }
			else {
				String[] arrRet = new String[2]; 
				arrRet[0] = "-104";
				arrRet[1] = "FP_ERROR_DEVICE_NOT_FOUND";
				return arrRet;
			}
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
	 }
	 
	 /**
	  * 关闭设备
	  * @return
	  */	
	 public String[] closeDevice(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 
			 iRet = FPIDeviceClose();
			 if(iRet == 0)
			 	FPICloseFpDev();
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = FPIGetErrorInfo(iRet);
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();

			 return arrRet;
		 }
		 
	 }
	 
	/**
	  * 指纹模板登记
	  * @return
	  */	
	 public String[] getFpVersion(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 byte[] psDevInfo = new byte[100];
			 int  iRet = -1;
			 
			 iRet = FPIGetVersion(psDevInfo);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[2];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psDevInfo).trim(); 
			
			 return arrRet;				 			 
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }		
	 }
	 
	 /**
	  * 获取设备序列号
	  * @return
	  */	
	 public String[] getDeviceID(){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 
			 byte[] psDeviceID = new byte[60];
			 int  iRet = -1;
			 
			 iRet  = FPIGetDeviceID(psDeviceID);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 
			 String[] arrRet = new String[2];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psDeviceID);
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }		
	 }
 
	 /**
	  * 指纹模板登记
	  * @return
	  */	
	 public String[] registerFinger(int nTimeout){
				  
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 byte []psTemplateBuf = new byte[1024];
			 int  []pnLength     = new int[2];
			 int  iRet = -1;
			
			 iRet = FPIGetTemplate(nTimeout, psTemplateBuf, pnLength);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[2];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psTemplateBuf).trim(); 
			
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
	 }
	 
	 /**
	  * 读取指纹特征
	  * @return
	  */
	 public String[] readFinger(int nTimeout){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{			 
			 byte []psFeatureBuf = new byte[1024];
			 int  []pnLength     = new int[2];
			 int  iRet = -1;
			
			 iRet = FPIGetFeature(nTimeout, psFeatureBuf, pnLength);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[2];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psFeatureBuf).trim(); 
			
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }
	 
	 /**
	  * 异步采集指纹
	  * @return
	  */
	 public String[] synReadFinger(int nIndex, String strTimeout){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int iret = FindAndOpenFpDev();
			 if (iret != 0)
				 Log.e(TAG, "FindUSBdevice {" + fd + "} fail");
			 else
				 Log.e(TAG, "FindUSBdevice {" + fd + "} ok"); 
			 if (iret == 0) {
				 String[] arrRet = new String[2];//gatherEnroll(fd, nIndex, Integer.parseInt(strTimeout));
				 FPICloseFpDev();
				 if (arrRet[0].equals("0")){
					 
					 String[] arr = new String[2];
					 arr[0] = arrRet[0];
					 arr[2] = arrRet[1];	
					 return arr;
					 
				 }
				 else
					 return arrRet;
				 //return getFeature(fd, Integer.parseInt(strTimeout));
			 }
			 else {
					
				 String[] arrRet = new String[2]; 
				 arrRet[0] = "1";
				 arrRet[1] = "未识别到指纹设备";
				 return arrRet;
			 }
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }	
	 
	 
	 /**
	  * 异步采集后进行合成
	  * @return
	  */
	 public String[] checkFinger(){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{			 
			 
			 int  iRet = -1;
			 iRet = FPICheckFinger();			 
			 if (iRet < 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[2];
			 arrRet[0] = Integer.toString(iRet);
			 if (iRet == 0){
				 arrRet[1] = "finger is down";
			 }
			 else{
				 arrRet[1] = "finger is up";
			 }
			 			 
			 return arrRet;
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }	
	 
	 /**
	  * 采集图像
	  * @return
	  */
	 public String[] getImage(int nTimeOut, String sBmpFile){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{	
			 byte[] psImage = new byte[IMAGE_LEN_BASE64];
			 int [] pnLength = new int[2];
			 byte[] psMAC = new byte[20];
			 int [] iIsValid = new int[2];
			 byte[] psBmpFile = sBmpFile.getBytes();
			 
			 int  iRet = -1;
			 iRet = FPIGetImage(nTimeOut, psImage, pnLength, psBmpFile);
			 if (iRet < 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[4];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psImage); 
			 arrRet[2] = Integer.toString(pnLength[0]);
			 arrRet[3] = Integer.toString(iIsValid[0]);			 	
			 
			 return arrRet;	
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }	
	 
	 
	 /**
	  * 获取指纹图像，并解析图像格式
	  * @return
	  */
	 public String[] getImageEx(int nTimeOut, String sBmpFile){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{	
			 byte[] psImage = new byte[IMAGE_LEN_BASE64];
			 int [] pnLength = new int[2];
			 byte[] psMAC = new byte[20];
			 int [] iIsValid = new int[2];
			 int [] iQuility = new int[2];
			 byte[] psBmpFile = sBmpFile.getBytes();
			 
			 int  iRet = -1;
			 iRet = FPIGetImageEx(nTimeOut, psImage, pnLength, iIsValid, iQuility, psBmpFile);
			 if (iRet < 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[6];
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psImage); 
			 arrRet[2] = Integer.toString(pnLength[0]);
			 arrRet[3] = new String(psMAC);
			 arrRet[4] = Integer.toString(iIsValid[0]);	
			 arrRet[5] = Integer.toString(iQuility[0]);	
			 
			 return arrRet;	
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }	

	 /**
	  * 取消操作
	  * @return
	  */
	 public String[] cancel(){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{	
			 
			 FPICancel();
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "0";
			 arrRet[1] = "success";
			 
			 return arrRet;
			 			 			
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
	 }	
	 
	 
	 public String[] getFeatureByImg(byte[] psImage){
		 
		 if (isLoadOK  != 0)
		 {
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 byte[] psVerBuf = new byte[1024];			 
			 int [] pnLength = new int[2];
			 
			 iRet = FPIGetFeatureByImg(psImage, psVerBuf, pnLength);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[3]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psVerBuf).trim();
			 arrRet[2] = Integer.toString(pnLength[0]);
			 
			 return arrRet;
			 			 			
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 
		 
	 }
	public String[] getTemplateByImg(String sImage1, String sImage2, String sImage3){
		
		if (isLoadOK  != 0)
		{
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 byte[] psRegBuf = new byte[1024];			 
			 int [] pnLength = new int[2];			 
			 
			 iRet = FPIGetTemplateByImg(sImage1.getBytes(), sImage2.getBytes(), sImage3.getBytes(),  psRegBuf, pnLength);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[3]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psRegBuf).trim();
			 arrRet[2] = Integer.toString(pnLength[0]);
			 
			 return arrRet;
			 			 			
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 	
	}
	
	public String[] fpVerify(String sMB, String sTZ, int nLevel){
		
		if (isLoadOK  != 0)
		{
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 byte[] psRegBuf = new byte[1024];			 
			 int [] pnLength = new int[2];			 
			 
			 iRet = FPIFpVerify(sMB.getBytes(), sTZ.getBytes(), nLevel);
			 String[] arrRet = new String[2]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = FPIGetErrorInfo(iRet);
			 
			 return arrRet;
			 			 			
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 	
	}
	
	public String[] getTemplateByTZ(String sTZ1, String sTZ2, String sTZ3){
		
		if (isLoadOK  != 0)
		{
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = sLoadErrMsg;
			 
			 return arrRet;
		 }
		 
		 try{
			 int  iRet = -1;
			 byte[] psRegBuf = new byte[1024];			 
			 int [] pnLength = new int[2];			 
			 
			 iRet = FPIEnrollX(sTZ1.getBytes(), sTZ2.getBytes(), sTZ3.getBytes(),  psRegBuf, pnLength);
			 if (iRet != 0){
				 String[] arrRet = new String[2]; 
				 arrRet[0] = Integer.toString(iRet);
				 arrRet[1] = FPIGetErrorInfo(iRet);
				 
				 return arrRet;
			 }
			 
			 String[] arrRet = new String[3]; 
			 arrRet[0] = Integer.toString(iRet);
			 arrRet[1] = new String(psRegBuf).trim();
			 arrRet[2] = Integer.toString(pnLength[0]);
			 
			 return arrRet;
			 			 			
		 }
		 catch(Throwable  ex){
			 
			 //IFpDevDriver.logger.error(ex.toString());
			 
			 String[] arrRet = new String[2]; 
			 arrRet[0] = "-101";
			 arrRet[1] = ex.toString();
			 
			 return arrRet;
		 }
		 	
	}

	/**
	 * 读取指纹特征，并上传指纹图像
	 * @return
	 */
	public String[] readFingerAndImage(int nTimeout, String sBmpFile){

		if (isLoadOK  != 0)
		{
			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = sLoadErrMsg;

			return arrRet;
		}

		try{
			byte[] psFeatureBuf = new byte[1024];
			int [] pnFeatureLen = new int[2];
			byte[] psImage = new byte[IMAGE_LEN_BASE64];
			int [] pnImageLen = new int[2];
			int [] iIsValid = new int[2];
			byte[] psBmpFile = sBmpFile.getBytes();
			int  iRet = -1;

			iRet = FPIGetFeatureAndImage(nTimeout, psFeatureBuf, pnFeatureLen, psImage, pnImageLen, psBmpFile);
			if (iRet != 0){
				String[] arrRet = new String[2];
				arrRet[0] = Integer.toString(iRet);
				arrRet[1] = FPIGetErrorInfo(iRet);

				return arrRet;
			}

			String[] arrRet = new String[5];
			arrRet[0] = Integer.toString(iRet);
			arrRet[1] = new String(psFeatureBuf).trim();
			arrRet[2] = new String(psImage).trim();
			arrRet[3] = Integer.toString(pnImageLen[0]);
			arrRet[4] = Integer.toString(iIsValid[0]);

			return arrRet;
		}
		catch(Throwable  ex){

			//IFpDevDriver.logger.error(ex.toString());

			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = ex.toString();

			return arrRet;
		}
	}

	/**
	 * 准备采集一副图像
	 * @return
	 */
	public String[] beginCapture(int nTimeOut){

		if (isLoadOK  != 0)
		{
			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = sLoadErrMsg;

			return arrRet;
		}

		try{
			byte[] psImage = new byte[IMAGE_LEN_BASE64];
			int [] pnLength = new int[2];
			byte[] psMAC = new byte[20];
			int [] iIsValid = new int[2];
			byte[] psBmpFile = sBmpFile.getBytes();

			int  iRet = -1;
			iRet = FPIBeginCapture(nTimeOut);
			if (iRet < 0){
				String[] arrRet = new String[2];
				arrRet[0] = Integer.toString(iRet);
				arrRet[1] = FPIGetErrorInfo(iRet);

				return arrRet;
			}

			String[] arrRet = new String[2];
			arrRet[0] = Integer.toString(iRet);
			arrRet[1] = FPIGetErrorInfo(iRet);

			return arrRet;
		}
		catch(Throwable  ex){

			//IFpDevDriver.logger.error(ex.toString());

			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = ex.toString();

			return arrRet;
		}
	}

	/**
	 * 采集图像
	 * @return
	 */
	public String[] getFPBmpData(String sBmpFile){

		if (isLoadOK  != 0)
		{
			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = sLoadErrMsg;

			return arrRet;
		}

		try{
			byte[] psImage = new byte[IMAGE_LEN_BASE64];
			int [] pnLength = new int[2];
			int [] iIsValid = new int[2];
			byte[] psBmpFile = sBmpFile.getBytes();

			int  iRet = -1;
			iRet = FPIGetFPBmpData(psImage, pnLength, psBmpFile);
			if (iRet < 0){
				String[] arrRet = new String[2];
				arrRet[0] = Integer.toString(iRet);
				arrRet[1] = FPIGetErrorInfo(iRet);

				return arrRet;
			}

			String[] arrRet = new String[3];
			arrRet[0] = Integer.toString(iRet);
			arrRet[1] = new String(psImage);
			arrRet[2] = Integer.toString(pnLength[0]);

			return arrRet;
		}
		catch(Throwable  ex){

			//IFpDevDriver.logger.error(ex.toString());

			String[] arrRet = new String[2];
			arrRet[0] = "-101";
			arrRet[1] = ex.toString();

			return arrRet;
		}
	}
}
	
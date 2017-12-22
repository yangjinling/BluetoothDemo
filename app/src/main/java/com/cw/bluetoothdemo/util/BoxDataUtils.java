package com.cw.bluetoothdemo.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.connection.SerialConnection;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yangjinling
 */
public class BoxDataUtils {
    public static final String KEY = "32323232323232323232323232323232";

    private static Handler h = new Handler();
    private static ProgressDialog mdialog;
    private static Dialog dialogs;

    /*****************************************************************************
     * 接口回调定义*
     *****************************************************************************/

    private static DataCallBack mDataCallBack;

    // 数据回调
    public interface DataCallBack {
        void onSuccess(String s);

        void onFail();
    }


    /*****************************************************************************
     * SDK*
     *****************************************************************************/


    /**
     * 获取版本号
     */
    public static void getData(final String code, final DataCallBack mCallBack) {
        mDataCallBack = mCallBack;
        checkReadThread();
        new Thread(new Runnable() {

            @Override
            public void run() {
                //TODO
                sendData(BJCWUtil.StrToHex(code));// 发送数据
            }
        }).start();
    }


    /*****************************************************************************
     * 封装方法*
     *****************************************************************************/

    // 3、获取imei
    // 指令：02 00 02 a8 03 00
    //
    // 返回数据：
    // 02 // stx
    // 00 1b // length
    // b8 // cmd
    // 00 // state
    // 30 35 64 36 66 66 33 36 33 38 33 37 35 30 34 36 34 33 32 35 35 38 33 35
    // // IMEI
    // 03 // etx
    // f3 // xor


    /**
     * 发送指令
     *
     * @param data
     * @return
     */
    public static void sendData(byte[] data) {
        try {
            SerialConnection connection = AppConfig.getInstance().
                    getConnection();
            if (null == connection) {
                Log.e("YJL", "串口无连接");
            } else {
                BufferedOutputStream out = new BufferedOutputStream(
                        connection.getOutputStream());
                Log.e("YJL", "发送了没有？？？");
                out.write(data);
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*****************************************************************************
     * 返回结果*
     *****************************************************************************/


    /**
     * 处理获取版本号返回的数据
     *
     * @param data
     */
    private static void handleResult(String data) {

        if (null != data) {

            mDataCallBack.onSuccess(data);
            mReadThread = null;
        } else {
            mDataCallBack.onFail();
        }

    }

    /*****************************************************************************
     * 开启读进程*
     *****************************************************************************/

    public static void checkReadThread() {
        if (mReadThread == null) {
            mReadThread = new ReadThread();
            Log.e("YJL", "开启读的进程");
            mReadThread.start();
        }
    }

    public static ReadThread mReadThread;

    static class ReadThread extends Thread {

        @Override
        public void run() {
            Integer len = null;
            SerialConnection connection = AppConfig.getInstance()
                    .getConnection();
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) { // 判断是否读取完毕
                // Tools.ShowSysout("----read--data-----");
                try {
                    if ((bytes = input.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        final String strReadDate = BJCWUtil.HexTostr(buf_data, buf_data.length);
                        Log.e("YJL", "串口到蓝牙的数据为：：：" + strReadDate);
//                        Thread.sleep(100);
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("YJL", "读完了");
                                notifyWriteThread(strReadDate);
                            }
                        }, 100);
//                        h.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.e("YJL", "读完了");
//                                notifyWriteThread(strReadDate);
//                            }
//                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("YJL", "异常");
                    // CBApplication.closeSerialPort();
//                    AppConfig.getInstance().closeSerialPort();
                    mReadThread = null;
                    break;
                }
            }
        }
    }

    private static void notifyWriteThread(String data) {
        Log.e("YJL", "接收到数据---->>" + data);
        if (TextUtils.isEmpty(data)) {
            return;
        }
        handleResult(data);
    }

}

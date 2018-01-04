package com.cw.bluetoothdemo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.cw.bluetoothdemo.app.Contents;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * 作者：杨金玲 on 2017/12/27 11:51
 * 邮箱：18363820101@163.com
 */

public class Control {
    public static void gpio_control(int gpio, int mode) {
        try {
            String file_name = new String("/sys/class/gpio/gpio" + gpio + "/value");
            File file = new File(file_name);
            if (file.exists() == false) {
                file_name = new String("/sys/class/gpiocontrol/gpiocontrol/gpiocontrol" + gpio);
                file = new File(file_name);
            }

            FileWriter localFileWriter = new FileWriter(file);
            if (mode == 1)
                localFileWriter.write("1");
            else if (mode == 0)
                localFileWriter.write("0");
            localFileWriter.close();
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }
    }

    public static void showDialog(String info, Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(info);
            builder.setTitle("提示");
            // builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setCancelable(false);
            builder.setPositiveButton("确认", null).create().show();
        }
    }

    public static void ShowToast(String info, Context context) {
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }

    public static byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        return datas;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {//图片所在SD卡的路径
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 100, 100);//自定义一个宽和高
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;//获取图片的高
        final int width = options.outWidth;//获取图片的框
        int inSampleSize = 4;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;//求出缩放值
    }

    /**
     * 将byte数组转换为字符串
     *
     * @param b byte数组
     * @return 字符串
     */
    public static String byteToStr(byte[] b) {
        if (b.length == 0)
            throw new NullPointerException("data is null!");
        int pos = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == 0) {
                pos = i;
                break;
            }
        }
        byte[] bstr = new byte[pos];
        System.arraycopy(b, 0, bstr, 0, pos);
        String str = new String(bstr);
        return str;
    }
    public static byte[] getStreamByFile(String path) {
        byte[] b = null;
        try {
            File f = new File(path);
            InputStream input = null;
            input = new FileInputStream(f);
            b = new byte[input.available()];
            input.read(b);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }



    public static byte[] intToByteArray(int i) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream dos= new DataOutputStream(buf);
        dos.writeInt(i);
        byte[] b = buf.toByteArray();
        dos.close();
        buf.close();
        return b;
    }

}

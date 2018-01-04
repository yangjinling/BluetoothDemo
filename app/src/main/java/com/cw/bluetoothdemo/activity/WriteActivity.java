package com.cw.bluetoothdemo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.view.DialogListener;
import com.cw.bluetoothdemo.view.MySignView;
import com.cw.bluetoothdemo.R;
import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.util.Control;
import com.cw.bluetoothdemo.view.WritePadDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 签名界面
 */
public class WriteActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_clear_write; // 清除笔迹
    private Button btn_confirm_write; // 完成签名
    private Button btn_trade_detail; // 交易详情
    private MySignView mySignView;
    private String orderNum;
    private String saleMessage;
    //	private String orderPng;
    private String order_sPng;
    private boolean isPortrait = true; // 是否是竖屏

    private boolean isSuccess = false;
    private Bitmap singBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        findViews();
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            isPortrait = true;
        } else {
            // 横屏
            isPortrait = false;
        }

        // 如果用户之前签字了，只不过横竖屏切换导致Activity重新加载，则界面显示用户之前的签字
    /*    if (AppConfig.mBitmap != null) {
            mySignView.post(new Runnable() {

                @Override
                public void run() {
                    if (AppConfig.mBitmap != null) {
                        mySignView.setBitmap(createBitmapBySize(
                                AppConfig.mBitmap,
                                mySignView.getWidth(),
                                mySignView.getHeight()));
                    }
                }
            });

            // 如果用户横竖屏切换导致activity重新绘制，那么设置用户之前画的笔画数
            mySignView.setTotalNum(AppConfig.getInstance().getUserWriteNum());
        }*/
    }

    /**
     * 获取控件
     */
    private void findViews() {
        btn_clear_write = (Button) findViewById(R.id.btn_clear_write);//清除笔记
        btn_clear_write.setOnClickListener(this);
        btn_confirm_write = (Button) findViewById(R.id.btn_confirm_write);//完成签名
        btn_confirm_write.setOnClickListener(this);

        mySignView = (MySignView) findViewById(R.id.mySignView);
//        singBitmap =mySignView.getBitmap();
    }

    private Bitmap createBitmapBySize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private Bitmap createBitmapForFotoMixBottom(Bitmap first, Bitmap second) {
        if (first == null) {
            return null;
        }
        if (second == null) {
            return first;
        }
        int fw = first.getWidth();
        int fh = first.getHeight();
        int sh = second.getHeight();
        if (fh < sh) {
            return null;
        }
        Bitmap newBitmap = null;
        newBitmap = Bitmap.createBitmap(fw, fh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(second, 0, fh - sh, null);
        return newBitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear_write:
                //清除笔记
                mySignView.empty();
                AppConfig.mBitmap = null;
                break;

            case R.id.btn_confirm_write:
                //完成签名
                Log.e("YJL", "mySignView.getTotalNum()----->>" + mySignView.getTotalNum());
                if (mySignView.isEmpty()) {
                    Toast.makeText(WriteActivity.this, "请先手写签名", Toast.LENGTH_SHORT).show();
                } else if (mySignView.getTotalNum() < 2) {
                    Control.showDialog("土豪：请再次秀一下正楷签名，以作凭证。", WriteActivity.this);
                } else {
                    // 设置屏幕横屏或者竖屏，防止提交签名时用户变换屏幕引起activity重绘
                    if (isPortrait) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);// 感应竖屏
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);// 感应横屏
                    }
//				if (noSignBean != null && (noSignBean.getV50freezestate() == 2 || noSignBean.getV50freezestate() == 5)) {
                /*if (noSignBean != null && noSignBean.getV50freezestate() == 6) {
                    LogUtil.e("xuetao", "签名失败的---补签字");
					new WriteTask(AppConfig.WEB_SaleSignSer_second).executeOnExecutor(Utils.FULL_TASK_EXECUTOR);
				} else {*/
                    Toast.makeText(WriteActivity.this, "签名成功", Toast.LENGTH_SHORT).show();
//                    savePNG_After(mySignView.getBitmap(), Contents.folderStr);
                    AppConfig.mBitmap = mySignView.getBitmap();
                    Intent intent = new Intent(WriteActivity.this, BluetoothServerActivity.class);
                    setResult(RESULT_OK, intent);
                    finish();
                    //}

                }
                break;


            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Control.ShowToast("尚未完成电子签名!", WriteActivity.this);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void savePNG_After(Bitmap bitmap, String name) {
        Contents.path = name + File.separator + System.currentTimeMillis() + ".jpg";
        File file = new File(Contents.path);
        Contents.file = file;
        try {
            FileOutputStream out = new FileOutputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] photo = outputStream.toByteArray();
            if (null != photo) {
                out.write(photo);
            }
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            Intent intent = new Intent(WriteActivity.this, BluetoothServerActivity.class);
            if (!mySignView.isEmpty()) {
                Log.e("YJL", "-------set---------");
                AppConfig.mBitmap = mySignView.getBitmap();
//                        // 保存用户已经画的笔画数
                AppConfig.getInstance().setUserWriteNum(mySignView.getTotalNum());
                setResult(RESULT_OK, intent);
                finish();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        if (!isSuccess) {
            Log.e("YJL", "-------destroy---------");
            if (!mySignView.isEmpty()) {
                Log.e("YJL", "-------set---------");
                AppConfig.mBitmap = mySignView.getBitmap();
                // 保存用户已经画的笔画数
                AppConfig.getInstance().setUserWriteNum(mySignView.getTotalNum());
            }
        }
        super.onDestroy();
    }
}

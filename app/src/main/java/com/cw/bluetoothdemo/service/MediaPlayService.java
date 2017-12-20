package com.cw.bluetoothdemo.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.cw.bluetoothdemo.app.Contents;

import java.io.File;

public class MediaPlayService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private static final String TAG = "MediaPlayService";
    public static MediaServiceListener mediaServiceListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " + intent.getStringExtra("type"));
        String str = intent.getStringExtra("type");
        Log.e(TAG, "文件名: " + str);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();

        } else {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        mediaPlayer.setOnCompletionListener(this);
        initMediaPlayer(str);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer(String name) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Bluetooth/video",
                    name + ".wav");
            Log.e(TAG, "initMediaPlayer: " + file.getPath());
            mediaPlayer.setDataSource(file.getPath()); // 指定音频文件的路径
            mediaPlayer.prepare(); // 让MediaPlayer进入到准备状态
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start(); // 开始播放
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopSelf();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        if (mediaServiceListener != null) {
            mediaServiceListener.mediaCompletion();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            Log.e(TAG, "mediaplayer");
        }

    }

    public static void setKeyServiceListener(MediaServiceListener aMediaServiceListener) {
        mediaServiceListener = aMediaServiceListener;
    }

    public interface MediaServiceListener {
        void mediaCompletion();
    }
}

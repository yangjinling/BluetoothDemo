package com.cw.bluetoothdemo.manager;

import android.content.Context;
import android.content.Intent;

import com.cw.bluetoothdemo.service.MediaPlayService;

public class MediaServiceManager {

	public static void startService(Context context, String name) {
		startService(context, name,null);
	}

	public static void startService(Context context, String name, MediaPlayService.MediaServiceListener listener) {
		Intent intent = new Intent(context, MediaPlayService.class);
		intent.putExtra("type", name);
		MediaPlayService.setKeyServiceListener(listener);
		context.startService(intent);
	}
	public static void stopService(Context context){
		Intent intent = new Intent(context, MediaPlayService.class);
		context.stopService(intent);
	}
}

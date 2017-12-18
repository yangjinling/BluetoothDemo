package com.cw.bluetoothdemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cw.bluetoothdemo.R;
import com.cw.bluetoothdemo.app.AppConfig;
import com.cw.bluetoothdemo.app.Contents;
import com.cw.bluetoothdemo.util.BJCWUtil;
import com.cw.bluetoothdemo.util.BoxDataUtils;
import com.cw.bluetoothdemo.util.SocketServerUtil;

public class WifiServerActivity extends AppCompatActivity {

    private EditText edit;
    private TextView get_text;
    private Button button;

    private SocketServerUtil server;
    private EditText et_port;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_wifi);
        edit = (EditText) findViewById(R.id.edit);
        get_text = (TextView) findViewById(R.id.get_text);
        button = (Button) findViewById(R.id.button);
        et_port = ((EditText) findViewById(R.id.et_port));
        btn_start = ((Button) findViewById(R.id.start));
        register();
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**socket服务端开始监听*/
                if (!SocketServerUtil.isClint) {
                    server = new SocketServerUtil(Integer.parseInt(et_port.getText().toString().trim()));
                    server.beginListen();
                }

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**socket发送数据*/
                if (null != server)
                    server.sendMessage(BJCWUtil.StrToHex("52000A76312E302E302E319000"));
            }
        });


    }

    private void register() {
        Log.e("YJL", "注册方法");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contents.TYPE_WIFI);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (Contents.TYPE_WIFI.equals(action)) {
                BoxDataUtils.getData(intent.getStringExtra(Contents.KEY_WIFI), new BoxDataUtils.DataCallBack() {
                    @Override
                    public void onSuccess(final String version) {

                    }

                    @Override
                    public void onFail() {

                    }
                });
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        AppConfig.getInstance().getSocketServerUtil().unregisterHandler();
        super.onDestroy();
    }
}

package com.cw.bluetoothdemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cw.bluetoothdemo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bluetooth;
    private Button wifi;
    private Intent intent;
    private Button ble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        bluetooth = ((Button) findViewById(R.id.bluetooth));
        wifi = ((Button) findViewById(R.id.wifi));
        ble = ((Button) findViewById(R.id.ble));
        ble.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        wifi.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bluetooth:
                intent = new Intent(MainActivity.this, BluetoothServerActivity.class);
                startActivity(intent);
                break;
            case R.id.wifi:
                intent = new Intent(MainActivity.this, WifiServerActivity.class);
                startActivity(intent);
                break;
            case R.id.ble:
                intent = new Intent(MainActivity.this, BleActivity.class);
                startActivity(intent);
                break;
        }
    }
}

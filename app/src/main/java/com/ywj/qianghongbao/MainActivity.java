package com.ywj.qianghongbao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button startBtn;
    private MyBroadcastReceiver myBroadcastReceiver;
    private TextView tv_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_state = (TextView) findViewById(R.id.tv_state);
        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //打开系统设置中辅助功能
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "找到"+getResources().getString(R.string.desc_name)+"，然后开启服务", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ywj.qianghongbao.isOpen");
        registerReceiver(myBroadcastReceiver,filter);
    }

    class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent!=null){
                boolean isOpen = intent.getBooleanExtra("isOpen",false);
                tv_state.setText(isOpen?"服务已开启":"服务未开启");
                tv_state.setTextColor(isOpen? getColor(R.color.colorPrimary):getColor(R.color.colorRed));
            }
        }
    }
}

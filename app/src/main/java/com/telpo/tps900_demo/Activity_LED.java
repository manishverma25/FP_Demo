package com.telpo.tps900_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.led.Led900;

public class Activity_LED extends Activity {
    TextView title_tv;
    Button btn_green_on,btn_green_off,btn_orange_on,btn_orange_off;
    Led900 led = new Led900(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("LED Test");

        btn_green_on=findViewById(R.id.btn_green_on);

        btn_green_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    led.on(2);
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_green_off=findViewById(R.id.btn_green_off);

        btn_green_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    led.off(2);
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_orange_on=findViewById(R.id.btn_orange_on);

        btn_orange_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    led.on(3);
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_orange_off=findViewById(R.id.btn_orange_off);

        btn_orange_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    led.off(3);
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}

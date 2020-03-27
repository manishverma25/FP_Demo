package com.telpo.tps900_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static com.telpo.tps900_demo.R.id.emv_all;

public class EMVActivity extends Activity {

    TextView title_tv;
    Button emv_all,emv_ic,emv_magnetic,paypass,paywave,pinpad_example,pinpad_des,pinpad_rsa,pinpad_dukpt,nfc_detect;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv);

        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("EMV Example");

        nfc_detect=findViewById(R.id.nfc_detect);
        nfc_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityNFC.class));
            }
        });

        pinpad_example=findViewById(R.id.pinpad_example);
        pinpad_example.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivitySimple.class));
            }
        });

        pinpad_des=findViewById(R.id.pinpad_des);
        pinpad_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityDes.class));
            }
        });

        pinpad_dukpt=findViewById(R.id.pinpad_dukpt);
        pinpad_dukpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityDUKPT.class));
            }
        });

        pinpad_rsa=findViewById(R.id.pinpad_rsa);
        pinpad_rsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityRsa.class));
            }
        });

        emv_magnetic=findViewById(R.id.emv_magnetic);
        emv_magnetic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,Activitymagnetic.class));
            }
        });

        emv_ic=findViewById(R.id.emv_ic);
        emv_ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityEMV_ic.class));
            }
        });

        paypass=findViewById(R.id.paypass);
        paypass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityTPPaypass.class));
            }
        });

        paywave=findViewById(R.id.paywave);
        paywave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,ActivityVisaPaywave.class));
            }
        });

        emv_all=findViewById(R.id.emv_all);
        emv_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EMVActivity.this,Activity_emvall.class));
            }
        });
    }
}
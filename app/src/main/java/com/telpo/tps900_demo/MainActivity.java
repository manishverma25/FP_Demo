package com.telpo.tps900_demo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.other.BeepManager;

import java.util.List;

public class MainActivity extends Activity {

    Button print_test,qrcode_verify,magnetic_card_btn,ic_card_btn,nfc_btn,psam,led_btn,emv_btn;
    private BeepManager mBeepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
       //initview
        print_test=findViewById(R.id.print_test);
        qrcode_verify=findViewById(R.id.qrcode_verify);
        magnetic_card_btn=findViewById(R.id.magnetic_card_btn);
        ic_card_btn=findViewById(R.id.ic_card_btn);
        nfc_btn=findViewById(R.id.nfc_btn);
        psam=findViewById(R.id.psam);
        led_btn=findViewById(R.id.led_btn);
        emv_btn=findViewById(R.id.emv_btn);

        mBeepManager = new BeepManager(this, R.raw.beep);

        print_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ActivityPrinter.class));
            }
        });
        qrcode_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPackage("com.telpo.tps550.api")) {
                    Intent intent = new Intent();
                    intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                    try {
                        startActivityForResult(intent, 0x124);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
                }
            }
        });
        magnetic_card_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MegneticActivity.class));
            }
        });
        ic_card_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SmarCardNewActivity.class));
            }
        });
        nfc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NfcActivity_tps900.class));
            }
        });
        psam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,PsamActivity.class));
            }
        });
        led_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Activity_LED.class));
            }
        });
        emv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,EMVActivity.class));
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeepManager.close();
        mBeepManager = null;
    }


    private boolean checkPackage(String packageName) {
        PackageManager manager = this.getPackageManager();
        Intent intent = new Intent().setPackage(packageName);
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if (infos == null || infos.size() < 1) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x124) {
            if (resultCode == 0) {
                if (data != null) {
                    mBeepManager.playBeepSoundAndVibrate();
                    String qrcode = data.getStringExtra("qrCode");
                    Toast.makeText(MainActivity.this, "Scan result:" + qrcode, Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                Toast.makeText(MainActivity.this, "Scan Failed", Toast.LENGTH_LONG).show();
            }
        }

    }
}

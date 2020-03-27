package com.telpo.tps900_demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.telpo.emv.EmvService;
import com.telpo.emv.util.StringUtil;
import com.telpo.pinpad.PinpadService;

public class ActivityRsa extends Activity {
    TextView title_tv;
    EditText et_rsareslut;
    Button bn_rsaInit,bn_rsawrtKey,bn_rsa,bn_rsaCheckKey,bn_rsaDelKey;
    Context context;
    StringBuffer logBuf = new StringBuffer("");
    int ret;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa);
        context = this;


        viewInit();
        appInit();

    }

    void appInit(){
        EmvService.Open(context);
        ret = EmvService.Device_Open(context);
        if( ret == 0 ){
            AppendDis("device open success");
        }else {
            AppendDis("device open falied:"+ret);
        }
    }

    void setbutton(boolean flag){
        bn_rsawrtKey.setEnabled(flag);
        bn_rsa.setEnabled(flag);
        bn_rsaCheckKey.setEnabled(flag);
        bn_rsaDelKey.setEnabled(flag);
    }

    void viewInit(){

        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("PINPAD RSA");
        et_rsareslut = (EditText) findViewById(R.id.et_rsareslut);

        bn_rsaInit = (Button) findViewById(R.id.bn_rsaInit);
        bn_rsawrtKey = (Button) findViewById(R.id.bn_rsawrtKey);
        bn_rsa = (Button) findViewById(R.id.bn_rsa);

        bn_rsaInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ret = PinpadService.Open(context);
                if( ret == 0 ){
                    setbutton(true);
                    AppendDis("Init success");
                }else {
                    setbutton(false);
                    AppendDis("Init falied:"+ret);
                }
            }
        });

        bn_rsaCheckKey = (Button) findViewById(R.id.bn_rsaCheckKey);
        bn_rsaCheckKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_rsakeyIndex = (EditText) findViewById(R.id.et_rsakeyIndex);
                int idx = Integer.parseInt(et_rsakeyIndex.getText().toString());
                ret = PinpadService.TP_PinpadCheckKey(PinpadService.KEY_TYPE_RSA,idx);
                AppendDis("check rsa key "+idx+" :"+ret);
            }
        });

        bn_rsaDelKey = (Button) findViewById(R.id.bn_rsaDelKey);
        bn_rsaDelKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_rsakeyIndex = (EditText) findViewById(R.id.et_rsakeyIndex);
                int idx = Integer.parseInt(et_rsakeyIndex.getText().toString());
                ret = PinpadService.TP_PinpadDeleteKey(PinpadService.KEY_TYPE_RSA,idx);
                AppendDis("delete rsa key "+idx+" :"+ret);
            }
        });

        bn_rsawrtKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_rsakeyIndex = (EditText) findViewById(R.id.et_rsakeyIndex);
                EditText et_rsaMkeyIndex = (EditText) findViewById(R.id.et_rsaMkeyIndex);
                Spinner sp_rsawrtmode = (Spinner) findViewById(R.id.sp_rsawrtmode);
                EditText et_rsaModule = (EditText) findViewById(R.id.et_rsaModule);
                EditText et_rsaExp = (EditText) findViewById(R.id.et_rsaExp);

                int keyindex = Integer.parseInt(et_rsakeyIndex.getText().toString());
                int mkindex = Integer.parseInt(et_rsaMkeyIndex.getText().toString());
                int mode = sp_rsawrtmode.getSelectedItemPosition();
                byte[] Module = StringUtil.hexStringToByte(et_rsaModule.getText().toString());
                byte[] Exponent = StringUtil.hexStringToByte(et_rsaExp.getText().toString());
                ret = PinpadService.TP_WriteRSAKey(keyindex,Module,Exponent,mode,mkindex);
                AppendDis("WriteRSAKey："+ret);
            }
        });

        bn_rsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_rsaIndex = (EditText) findViewById(R.id.et_rsaIndex);
                EditText et_rsaData = (EditText) findViewById(R.id.et_rsaData);
                Spinner sp_paddingmode = (Spinner) findViewById(R.id.sp_paddingmode);

                int index = Integer.parseInt(et_rsaIndex.getText().toString());
                int pkc = sp_paddingmode.getSelectedItemPosition() +1;
                byte[] data = StringUtil.hexStringToByte(et_rsaData.getText().toString());
                byte[] out = new byte[128];
                Log.w("RSA", "date: " + StringUtil.bytesToHexString(data));
                ret = PinpadService.TP_PinpadRSAbyKeyIndex(index,data,out,pkc);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("RSA error："+ret);
                    return;
                }else{
                    Log.w("RSA", "RSA: " + StringUtil.bytesToHexString(out));
                    AppendDis("RSA :"+ StringUtil.bytesToHexString(out));
                }
            }
        });
    }


    void ClearDis(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_rsareslut.setText("");
                logBuf = new StringBuffer("");
            }
        });
    }
    void AppendDis(String Mes){
        logBuf.append(Mes);
        logBuf.append("\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_rsareslut.requestFocus();
                et_rsareslut.setText(logBuf.toString());
                et_rsareslut.setSelection(et_rsareslut.getText().length());
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EmvService.deviceClose();
    }

}

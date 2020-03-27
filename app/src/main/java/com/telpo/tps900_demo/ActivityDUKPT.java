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
import com.telpo.pinpad.PinParam;
import com.telpo.pinpad.PinpadService;

public class ActivityDUKPT extends Activity {
    TextView title_tv;
    Context context;
    StringBuffer logBuf = new StringBuffer("");
    int ret;
    EditText et_dukptreslut;
    Button bn_dukptInit,bn_dukptwrtbdk,bn_dukptwrtipek,bn_dukptSetksn,bn_dukptstart,bn_dukptend,
            bn_dukptMAC,bn_dukptDES,bn_dukptCheckKey,bn_dukptDelKey,bn_dukptgetpin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukpt);
        context = this;
        viewInit();
        appInit();
    }
    void setbutton(boolean flag){
        bn_dukptwrtbdk.setEnabled(flag);
        bn_dukptwrtipek.setEnabled(flag);
        bn_dukptstart.setEnabled(flag);
        bn_dukptSetksn.setEnabled(flag);
        bn_dukptCheckKey.setEnabled(flag);
        bn_dukptDelKey.setEnabled(flag);


    }

    void viewInit(){

        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("PINPAD DUKPT");
        et_dukptreslut = (EditText) findViewById(R.id.et_dukptreslut);
        bn_dukptInit = (Button) findViewById(R.id.bn_dukptInit);
        bn_dukptwrtbdk = (Button) findViewById(R.id.bn_dukptwrtbdk);
        bn_dukptwrtipek = (Button) findViewById(R.id.bn_dukptwrtipek);
        bn_dukptSetksn = (Button) findViewById(R.id.bn_dukptSetksn);
        bn_dukptstart = (Button) findViewById(R.id.bn_dukptstart);
        bn_dukptend = (Button) findViewById(R.id.bn_dukptend);
        bn_dukptMAC = (Button) findViewById(R.id.bn_dukptMAC);
        bn_dukptDES = (Button) findViewById(R.id.bn_dukptDES);
        bn_dukptgetpin = (Button) findViewById(R.id.bn_dukptgetpin);

        bn_dukptInit.setOnClickListener(new View.OnClickListener() {
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

        bn_dukptwrtbdk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_dukptIndex  = (EditText) findViewById(R.id.et_dukptIndex);
                EditText et_dukptMkeyIndex  = (EditText) findViewById(R.id.et_dukptMkeyIndex);
                EditText et_dukptbdk  = (EditText) findViewById(R.id.et_dukptbdk);
                EditText et_dukptksn  = (EditText) findViewById(R.id.et_dukptksn);
                Spinner sp_dukptwrtmode = (Spinner) findViewById(R.id.sp_dukptwrtmode);

                int index = Integer.parseInt(et_dukptIndex.getText().toString());
                int mkindex = Integer.parseInt(et_dukptMkeyIndex.getText().toString());
                int mode = sp_dukptwrtmode.getSelectedItemPosition();
                byte[] bdk = StringUtil.hexStringToByte(et_dukptbdk.getText().toString());
                byte[] ksn = StringUtil.hexStringToByte(et_dukptksn.getText().toString());

                ret = PinpadService.TP_PinpadWriteDukptKey(bdk,ksn,index,mode,mkindex);
                AppendDis("WriteDukptKey："+ret);
            }
        });

        bn_dukptwrtipek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_dukptIndex  = (EditText) findViewById(R.id.et_dukptIndex);
                EditText et_dukptMkeyIndex  = (EditText) findViewById(R.id.et_dukptMkeyIndex);
                EditText et_dukptipek  = (EditText) findViewById(R.id.et_dukptipek);
                EditText et_dukptksn  = (EditText) findViewById(R.id.et_dukptksn);
                Spinner sp_dukptwrtmode = (Spinner) findViewById(R.id.sp_dukptwrtmode);

                int index = Integer.parseInt(et_dukptIndex.getText().toString());
                int mkindex = Integer.parseInt(et_dukptMkeyIndex.getText().toString());
                int mode = sp_dukptwrtmode.getSelectedItemPosition();
                byte[] ipek = StringUtil.hexStringToByte(et_dukptipek.getText().toString());
                byte[] ksn = StringUtil.hexStringToByte(et_dukptksn.getText().toString());

                ret = PinpadService.TP_PinpadWriteDukptIPEK(ipek,ksn,index,mode,mkindex);
                AppendDis("WriteDukptIPEK："+ret);
            }
        });

        bn_dukptSetksn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_dukptIndex  = (EditText) findViewById(R.id.et_dukptIndex);
                EditText et_dukptksn  = (EditText) findViewById(R.id.et_dukptksn);
                int index = Integer.parseInt(et_dukptIndex.getText().toString());
                byte[] ksn = StringUtil.hexStringToByte(et_dukptksn.getText().toString());
                ret = PinpadService.TP_PinpadDukptSetKSN(index,ksn);
                AppendDis("WriteDukptIPEK："+ret);
            }
        });

        bn_dukptCheckKey = (Button) findViewById(R.id.bn_dukptCheckKey);
        bn_dukptCheckKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_dukptIndex  = (EditText) findViewById(R.id.et_dukptIndex);
                int idx = Integer.parseInt(et_dukptIndex.getText().toString());
                ret = PinpadService.TP_PinpadCheckKey(PinpadService.KEY_TYPE_DUKPT,idx);
                AppendDis("check dukpt key "+idx+" :"+ret);
            }
        });

        bn_dukptDelKey = (Button) findViewById(R.id.bn_dukptDelKey);
        bn_dukptDelKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_dukptIndex  = (EditText) findViewById(R.id.et_dukptIndex);
                int idx = Integer.parseInt(et_dukptIndex.getText().toString());
                ret = PinpadService.TP_PinpadDeleteKey(PinpadService.KEY_TYPE_DUKPT,idx);
                AppendDis("delete dukpt key "+idx+" :"+ret);
            }
        });

        bn_dukptstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_sessionIndex = (EditText) findViewById(R.id.et_sessionIndex);
                int idx = Integer.parseInt(et_sessionIndex.getText().toString());
                ret = PinpadService.TP_PinpadDukptSessionStart(idx);
                AppendDis("SessionStart："+ret);
                if(ret == PinpadService.PIN_OK){
                    bn_dukptend.setEnabled(true);
                    bn_dukptMAC.setEnabled(true);
                    bn_dukptDES.setEnabled(true);
                    bn_dukptgetpin.setEnabled(true);
                }
            }
        });

        bn_dukptend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ret = PinpadService.TP_PinpadDukptSessionEnd();
                AppendDis("SessionEnd："+ret);
                if(ret == PinpadService.PIN_OK){
                    bn_dukptend.setEnabled(false);
                    bn_dukptMAC.setEnabled(false);
                    bn_dukptDES.setEnabled(false);
                    bn_dukptgetpin.setEnabled(false);
                }
            }
        });

        bn_dukptMAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_sessionIndex = (EditText) findViewById(R.id.et_sessionIndex);
                int idx = Integer.parseInt(et_sessionIndex.getText().toString());
                EditText et_dukptMACdata = (EditText) findViewById(R.id.et_dukptMACdata);
                byte[] data = StringUtil.hexStringToByte(et_dukptMACdata.getText().toString());

                byte[] out = new byte[8];
                byte[] outKSN = new byte[10];
                ret = PinpadService.TP_PinpadDukptGetMac(data,out,outKSN);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("dukpt mac error："+ret);
                }else{
                    AppendDis("dukpt mac："+ StringUtil.bytesToHexString(out));
                    AppendDis("current KSN："+ StringUtil.bytesToHexString(outKSN));
                    Log.w("dukpt", "dukpt mac："+ StringUtil.bytesToHexString(out));
                }
            }
        });

        bn_dukptDES.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_sessionIndex = (EditText) findViewById(R.id.et_sessionIndex);
                int idx = Integer.parseInt(et_sessionIndex.getText().toString());
                Spinner sp_dukptDESmode = (Spinner) findViewById(R.id.sp_dukptDESmode);
                int mode  = sp_dukptDESmode.getSelectedItemPosition();
                EditText et_dukptMACdata = (EditText) findViewById(R.id.et_dukptMACdata);
                byte[] data = StringUtil.hexStringToByte(et_dukptMACdata.getText().toString());

                byte[] out = new byte[data.length];
                byte[] outKSN = new byte[10];
                if( data.length % 8 != 0 ){
                    AppendDis("len of Data must be divisible by 8 ");
                    return;
                }
                ret = PinpadService.TP_PinpadDukptDes(data,out,outKSN,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("dukpt mac error："+ret);
                    return;
                }else{
                    AppendDis("dukpt DES："+ StringUtil.bytesToHexString(out));
                    AppendDis("current KSN："+ StringUtil.bytesToHexString(outKSN));
                    Log.w("dukpt", "dukpt DES："+ StringUtil.bytesToHexString(out));
                }
                byte[] temp = new byte[out.length];
                mode = (mode +1) %2;
                ret = PinpadService.TP_PinpadDukptDes(out,temp,outKSN,mode);
                if(StringUtil.bytesToHexString(temp).equals(et_dukptMACdata.getText().toString())){
                    AppendDis("data match ");
                }else{
                    AppendDis("data not match："+ StringUtil.bytesToHexString(temp));
                    Log.w("dukpt", "not match："+ StringUtil.bytesToHexString(temp));
                }
            }
        });

        bn_dukptgetpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Spinner sp_dukpt_pin_block_format = (Spinner) findViewById(R.id.sp_dukpt_pin_block_format);
                Spinner sp_dukpt_pin_is_show_pan = (Spinner) findViewById(R.id.sp_dukpt_pin_is_show_pan);
                EditText et_dukpt_pin_pan  = (EditText) findViewById(R.id.et_dukpt_pin_pan);

                PinParam pinParam = new PinParam(context);

                pinParam.PinBlockFormat = sp_dukpt_pin_block_format.getSelectedItemPosition();
                pinParam.IsShowCardNo = sp_dukpt_pin_is_show_pan.getSelectedItemPosition();
                pinParam.CardNo=et_dukpt_pin_pan.getText().toString();
                pinParam.Amount="123.45";
                pinParam.MinPinLen = 4;
                pinParam.MaxPinLen = 12;
                pinParam.WaitSec = 60;
                Log.w("pin", "start to TP_PinpadDukptGetPin mode"+ pinParam.PinBlockFormat);
                ret = PinpadService.TP_PinpadDukptGetPin(pinParam);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("GetPin error："+ret);
                    return ;
                }else{
                    AppendDis("PinBlock ："+ StringUtil.bytesToHexString(pinParam.Pin_Block));
                    AppendDis("current KSN："+ StringUtil.bytesToHexString(pinParam.Curr_KSN));
                    Log.w("pin", "PinBlock ："+ StringUtil.bytesToHexString(pinParam.Pin_Block));
                    Log.w("pin", "current KSN："+ StringUtil.bytesToHexString(pinParam.Curr_KSN));
                }
            }
        });
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

    void ClearDis(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_dukptreslut.setText("");
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
                et_dukptreslut.requestFocus();
                et_dukptreslut.setText(logBuf.toString());
                et_dukptreslut.setSelection(et_dukptreslut.getText().length());
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EmvService.deviceClose();
    }
}

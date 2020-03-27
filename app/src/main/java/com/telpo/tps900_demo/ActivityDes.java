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

import java.util.Timer;
import java.util.TimerTask;

public class ActivityDes extends Activity {
    /*
     示例说明：（用""的数据都是用十六进制表示）使用：一键设置MK PIK DEK MAK AESK
                主密钥：sMasterKey = "30313233343536373839414243444546";
                Pin密钥：明文为："31323334353637383941424344454630"， 主密钥加密(3DES-ECB)得到sPinKey = "523968856F280E4492E4D114075D6A5E";
                Des密钥：明文为："32333435363738394142434445463031"， 主密钥加密(3DES-ECB)得到sDesKey = "AB12B6E24A3A2614F07D23D055DF5C16";
                Mac密钥：明文为："33343536373839414243444546303132"， 主密钥加密(3DES-ECB)得到sMacKey = "43591E440176D75044C015DF2E7F8BC5";
                Aes密钥：明文为："33435363738394142434445463031323"， 主密钥加密(3DES-ECB)得到sAesKey = "93152E266957B37959925589D7085B70";

            Des加密 “0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12” 结果：“0xB0, 0x44, 0x0D, 0x4C, 0x9A, 0x96, 0xC7, 0x09, 0xE7, 0xD7, 0x5F, 0x7E, 0xD0, 0xE0, 0xAD, 0xCA”
            Mac X99 计算 “0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12” 结果：“0x5f, 0x94, 0xe1, 0xb0, 0xc7, 0x84, 0xa1, 0x67”
            Aes ECB 计算“0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12” IV：“0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12,0x34,0x56,0x78,0x90,0x12”
                            结果：“0x9B, 0x9B, 0xE3, 0xD5, 0x41, 0xCF, 0x10, 0xC4, 0xC1, 0x34, 0xE7, 0x23, 0x1D, 0xE2, 0x53, 0x8E”
            pin： 卡号：6221234567890123456  密码：123456   结果：“0x3f, 0xc4, 0x24, 0x69, 0x8b, 0x1f, 0xa9, 0x40”
     */
    Context context;
    Spinner sp_keytype,sp_wrtmode;
    Button bn_Init,bn_wrtKey,bn_format,bn_des,bn_mac,bn_aes,
            bn_desCheckKey,bn_desDelKey,bn_desgetpin;

    Button bn_OneKeySet;    //一键设置MK PIK DEK MAK AESK
    EditText et_reslut,et_keyIndex,et_MkeyIndex,et_KeyData;
    StringBuffer logBuf = new StringBuffer("");
    int ret;
    int keyindex,mkeyindex,wrtmode,keytype;

    int nMKIndex = 0;
    int nPKIndex = 1;
    int nDEKIndex = 2;
    int nMACKIndex = 3;
    int nAESKIndex = 4;

    TextView title_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_des);
        context = this;
        viewInit();
        appInit();
    }
    void setbutton(boolean flag){
        bn_wrtKey.setEnabled(flag);
        bn_des.setEnabled(flag);
        bn_mac.setEnabled(flag);
        bn_aes.setEnabled(flag);
        bn_desCheckKey.setEnabled(flag);
        bn_desDelKey.setEnabled(flag);
        bn_desgetpin.setEnabled(flag);
        bn_OneKeySet.setEnabled(flag);
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

    void viewInit(){
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("PINPAD DES");
        et_reslut = (EditText) findViewById(R.id.et_reslut);
        et_keyIndex = (EditText) findViewById(R.id.et_keyIndex);
        et_MkeyIndex = (EditText) findViewById(R.id.et_MkeyIndex);
        //et_wrtMode = (EditText) findViewById(R.id.et_wrtMode);
        et_KeyData = (EditText) findViewById(R.id.et_KeyData);
        sp_keytype = (Spinner) findViewById(R.id.sp_keytype);
        sp_wrtmode = (Spinner) findViewById(R.id.sp_wrtmode);

        bn_desCheckKey = (Button) findViewById(R.id.bn_desCheckKey);
        bn_desCheckKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = Integer.parseInt(et_keyIndex.getText().toString());
                ret = PinpadService.TP_PinpadCheckKey(PinpadService.KEY_TYPE_NORMAL,idx);
                AppendDis("check normal key "+idx+" :"+ret);
            }
        });

        bn_desDelKey = (Button) findViewById(R.id.bn_desDelKey);
        bn_desDelKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int idx = Integer.parseInt(et_keyIndex.getText().toString());
                ret = PinpadService.TP_PinpadDeleteKey(PinpadService.KEY_TYPE_NORMAL,idx);
                AppendDis("delete normal key "+idx+" :"+ret);
            }
        });

        //一键设置MK PIK DEK MAK AESK测试密钥
        bn_OneKeySet = (Button)findViewById(R.id.bn_oneKeySet);
        bn_OneKeySet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                主密钥：sMasterKey = "30313233343536373839414243444546";
//                Pin密钥：明文为："31323334353637383941424344454630"， 主密钥加密(3DES-ECB)得到sPinKey = "523968856F280E4492E4D114075D6A5E";
//                Des密钥：明文为："32333435363738394142434445463031"， 主密钥加密(3DES-ECB)得到sDesKey = "AB12B6E24A3A2614F07D23D055DF5C16";
//                Mac密钥：明文为："33343536373839414243444546303132"， 主密钥加密(3DES-ECB)得到sMacKey = "43591E440176D75044C015DF2E7F8BC5";
//                Aes密钥：明文为："33435363738394142434445463031323"， 主密钥加密(3DES-ECB)得到sAesKey = "93152E266957B37959925589D7085B70";

                String sMasterKey = "30313233343536373839414243444546";

                String sPinKey = "31323334353637383941424344454630";
                String sDesKey = "32333435363738394142434445463031";
                String sMacKey = "33343536373839414243444546303132";
                String sAesKey = "33435363738394142434445463031323";
                String sPinKey_ciphertext = "523968856F280E4492E4D114075D6A5E";
                String sDesKey_ciphertext = "AB12B6E24A3A2614F07D23D055DF5C16";
                String sMacKey_ciphertext = "43591E440176D75044C015DF2E7F8BC5";
                String sAesKey_ciphertext = "93152E266957B37959925589D7085B70";

                //此处sMasterKey是明文，所以用PinpadService.KEY_WRITE_DIRECT 直接写入；
                ret = PinpadService.TP_WriteMasterKeyEx(nMKIndex, StringUtil.hexStringToByte(sMasterKey), PinpadService.KEY_WRITE_DIRECT,nMKIndex);
                AppendDis("TP_WriteMasterKeyEx("+nMKIndex+","+sMasterKey+","+ PinpadService.KEY_WRITE_DIRECT+","+nMKIndex+")-"+(ret== PinpadService.PIN_OK?"success":"fail"));
                //写入PinKey
                ret = PinpadService.TP_WritePinKey(nPKIndex, StringUtil.hexStringToByte(sPinKey_ciphertext), PinpadService.KEY_WRITE_DECRYPT, nMKIndex);
                AppendDis("TP_WritePinKey("+nPKIndex+","+sPinKey_ciphertext+","+ PinpadService.KEY_WRITE_DECRYPT+","+nMKIndex+")-"+(ret== PinpadService.PIN_OK?"success":"fail"));
                //或者
                //PinpadService.TP_WritePinKey(nPKIndex, StringUtil.hexStringToByte(sPinKey), PinpadService.KEY_WRITE_DIRECT, nAESKIndex);

                // 写入desKey
                ret = PinpadService.TP_WriteDesKey(nDEKIndex, StringUtil.hexStringToByte(sDesKey_ciphertext),  PinpadService.KEY_WRITE_DECRYPT, nMKIndex);
                AppendDis("TP_WriteDesKey("+nDEKIndex+","+sDesKey_ciphertext+","+ PinpadService.KEY_WRITE_DECRYPT+","+nMKIndex+")-"+(ret== PinpadService.PIN_OK?"success":"fail"));
                //或者
                //PinpadService.TP_WriteDesKey(nDEKIndex, StringUtil.hexStringToByte(sDesKey), PinpadService.KEY_WRITE_DIRECT, nAESKIndex);

                // 写入MacKey
                ret = PinpadService.TP_WriteMacKey(nMACKIndex, StringUtil.hexStringToByte(sMacKey_ciphertext),  PinpadService.KEY_WRITE_DECRYPT, nMKIndex);
                AppendDis("TP_WriteMacKey("+nMACKIndex+","+sMacKey_ciphertext+","+ PinpadService.KEY_WRITE_DECRYPT+","+nMKIndex+")-"+(ret== PinpadService.PIN_OK?"success":"fail"));
                //或者
                //PinpadService.TP_WriteMacKey(nMACKIndex, StringUtil.hexStringToByte(sMacKey), PinpadService.KEY_WRITE_DIRECT, nAESKIndex);

                // 写入AesKey
                ret = PinpadService.TP_WriteAESKey(nAESKIndex, StringUtil.hexStringToByte(sAesKey_ciphertext),  PinpadService.KEY_WRITE_DECRYPT, nMKIndex);
                AppendDis("TP_WriteAESKey("+nAESKIndex+","+sAesKey_ciphertext+","+ PinpadService.KEY_WRITE_DECRYPT+","+nMKIndex+")-"+(ret== PinpadService.PIN_OK?"success":"fail"));
                //或者
                //PinpadService.TP_WriteMacKey(nAESKIndex, StringUtil.hexStringToByte(sAesKey), PinpadService.KEY_WRITE_DIRECT, nAESKIndex);
            }
        });
        bn_Init = (Button) findViewById(R.id.bn_Init);
        bn_Init.setOnClickListener(new View.OnClickListener() {
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

        bn_format = (Button) findViewById(R.id.bn_format);
        bn_format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ret = PinpadService.TP_PinpadFormat(context);
                AppendDis("Format："+ret);
            }
        });

        bn_wrtKey = (Button) findViewById(R.id.bn_wrtKey);
        bn_wrtKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyindex = Integer.parseInt(et_keyIndex.getText().toString());
                mkeyindex = Integer.parseInt(et_MkeyIndex.getText().toString());
                wrtmode = sp_wrtmode.getSelectedItemPosition();
                keytype = sp_keytype.getSelectedItemPosition();

                byte[] keydata = StringUtil.hexStringToByte(et_KeyData.getText().toString());
                switch (keytype){
                    case 0:
                        ret = PinpadService.TP_WriteMasterKeyEx(keyindex,keydata,wrtmode,mkeyindex);
                        AppendDis("WriteMasterKeyEx："+ret);
                        break;

                    case 1:
                        ret = PinpadService.TP_WritePinKey(keyindex,keydata,wrtmode,mkeyindex);
                        AppendDis("WritePinKey："+ret);
                        break;

                    case 2:
                        ret = PinpadService.TP_WriteDesKey(keyindex,keydata,wrtmode,mkeyindex);
                        AppendDis("WriteDesKey："+ret);
                        break;

                    case 3:
                        ret = PinpadService.TP_WriteMacKey(keyindex,keydata,wrtmode,mkeyindex);
                        AppendDis("WriteMacKey："+ret);
                        break;

                    case 4:
                        ret = PinpadService.TP_WriteAESKey(keyindex,keydata,wrtmode,mkeyindex);
                        AppendDis("WriteAESKey："+ret);
                        break;

                    default:
                        break;
                }

            }
        });

        bn_des = (Button) findViewById(R.id.bn_des);
        bn_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_deskeyIndex = (EditText) findViewById(R.id.et_deskeyIndex);
                EditText et_desData = (EditText) findViewById(R.id.et_desData);
                Spinner sp_desmode = (Spinner) findViewById(R.id.sp_desmode);
                int idx = Integer.parseInt(et_deskeyIndex.getText().toString());
                int mode = sp_desmode.getSelectedItemPosition();
                byte[] keydata = StringUtil.hexStringToByte(et_desData.getText().toString());
                if(keydata.length % 8 != 0){
                    AppendDis("len of Data must be divisible by 8 ");
                    return ;
                }
                byte[] out = new byte[keydata.length];
                ret = PinpadService.TP_DesByKeyIndex(idx,keydata,out,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("Des error："+ret);
                    return;
                }else{
                    AppendDis("Des result"+idx+"："+ StringUtil.bytesToHexString(out));
                }
                byte[] Inverse = new byte[keydata.length];
                mode = ( mode+1)%2;
                ret = PinpadService.TP_DesByKeyIndex(idx,out,Inverse,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("Des error："+ret);
                    return;
                }else{
                    AppendDis("Inverse :"+ StringUtil.bytesToHexString(Inverse));
                }

            }
        });

        bn_mac = (Button) findViewById(R.id.bn_mac);
        bn_mac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_mackeyIndex = (EditText) findViewById(R.id.et_mackeyIndex);
                EditText et_macData = (EditText) findViewById(R.id.et_macData);
                Spinner sp_macmode = (Spinner) findViewById(R.id.sp_macmode);
                int idx = Integer.parseInt(et_mackeyIndex.getText().toString());
                int mode = sp_macmode.getSelectedItemPosition();
                byte[] keydata = StringUtil.hexStringToByte(et_macData.getText().toString());
                byte[] out = new byte[8];
                ret = PinpadService.TP_PinpadGetMac(idx,keydata,out,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("MAC error："+ret);
                }else{
                    AppendDis("MAC result："+ StringUtil.bytesToHexString(out));
                }
            }
        });

        bn_aes = (Button) findViewById(R.id.bn_aes);
        bn_aes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_aeskeyIndex = (EditText) findViewById(R.id.et_aeskeyIndex);
                EditText et_aesData = (EditText) findViewById(R.id.et_aesData);
                EditText et_aesIV = (EditText) findViewById(R.id.et_aesIV);
                Spinner sp_aesmode = (Spinner) findViewById(R.id.sp_aesmode);
                Spinner sp_aesalgm = (Spinner) findViewById(R.id.sp_aesalgm);
                int idx = Integer.parseInt(et_aeskeyIndex.getText().toString());
                int mode = sp_aesmode.getSelectedItemPosition();
                int algm = sp_aesalgm.getSelectedItemPosition();
                byte[] keydata = StringUtil.hexStringToByte(et_aesData.getText().toString());
                byte[] iv = StringUtil.hexStringToByte(et_aesIV.getText().toString());
                byte[] out ;

                if( algm != PinpadService.AES_ECB ){
                    if(iv.length != 16){
                        AppendDis("len of iv must be 16 bytes in algm CBC or OFB or CFB");
                        return ;
                    }
                }

                if( algm == PinpadService.AES_ECB ||  algm == PinpadService.AES_CBC ){
                    if(keydata.length % 16 != 0){
                        AppendDis("len of Data must be divisible by 16 in algm ECB or CBC ");
                        return ;
                    }
                }

                out = new byte[keydata.length];
                ret = PinpadService.TP_PinpadAESbyKeyIndex(idx,keydata,out,iv,algm,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("AES error："+ret);
                    return ;
                }else{
                    AppendDis("AES result"+idx+"："+ StringUtil.bytesToHexString(out));
                    Log.w("aes","AES result："+ StringUtil.bytesToHexString(out));
                }
                byte[] Inverse = new byte[keydata.length];
                mode = ( mode+1 ) % 2;
                ret = PinpadService.TP_PinpadAESbyKeyIndex(idx,out,Inverse,iv,algm,mode);
                if( ret != PinpadService.PIN_OK){
                    AppendDis("Inverse error："+ret);
                    return ;
                }else{
                    AppendDis("Inverse ："+ StringUtil.bytesToHexString(Inverse));
                    Log.w("aes","Inverse："+ StringUtil.bytesToHexString(Inverse));
                }
            }
        });

        bn_desgetpin = (Button) findViewById(R.id.bn_desgetpin);
        bn_desgetpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EditText et_pik_Index  = (EditText) findViewById(R.id.et_pik_Index);
                        Spinner sp_pin_block_format = (Spinner) findViewById(R.id.sp_pin_block_format);
                        Spinner sp_pin_is_show_pan = (Spinner) findViewById(R.id.sp_pin_is_show_pan);
                        EditText et_pin_pan  = (EditText) findViewById(R.id.et_pin_pan);

                        PinParam pinParam = new PinParam(context);
                        pinParam.KeyIndex = Integer.parseInt(et_pik_Index.getText().toString());
                        pinParam.PinBlockFormat = sp_pin_block_format.getSelectedItemPosition();
                        pinParam.IsShowCardNo = sp_pin_is_show_pan.getSelectedItemPosition();
                        pinParam.CardNo=et_pin_pan.getText().toString();
                        pinParam.Amount="123.45";
                        pinParam.MinPinLen = 4;
                        pinParam.MaxPinLen = 12;
                        pinParam.WaitSec = 60;
                        Log.w("pin", "start to TP_PinpadGetPin" );
                        //ret = PinpadService.TP_PinpadGetPin(pinParam);
                        ret = PinpadService.TP_PinpadGetPin(pinParam);
                        if( ret != PinpadService.PIN_OK){
                            AppendDis("GetPin error："+ret);
                            return ;
                        }else{
                            AppendDis("PinBlock ："+ StringUtil.bytesToHexString(pinParam.Pin_Block));
                            Log.w("pin", "PinBlock ："+ StringUtil.bytesToHexString(pinParam.Pin_Block));
                        }
                    }
                }).start();

                if(true)
                {
                    Timer tTimer = new Timer();
                    tTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            PinpadService.TP_PinpadGetPinExit();
                        }
                    },1000*5);
                }

            }
        });



    }


    void ClearDis(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_reslut.setText("");
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
                et_reslut.requestFocus();
                et_reslut.setText(logBuf.toString());
                et_reslut.setSelection(et_reslut.getText().length());
                et_reslut.clearFocus();

            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EmvService.deviceClose();
    }
}

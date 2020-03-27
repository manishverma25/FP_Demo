package com.telpo.tps900_demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.emv.EmvService;
import com.telpo.pinpad.PinParam;
import com.telpo.pinpad.PinpadService;
import com.telpo.util.StringUtil;

/*
 示例说明：（数据都是用十六进制表示）
        主密钥：sMasterKey = "30313233343536373839414243444546";
        Pin密钥：明文为："32323232323232323131313131313131"， 主密钥加密(3DES-ECB)得到sPinKey = "50B55FE757865000498C189C17F5E377";
        Des密钥：明文为："31313131313131313232323232323232"， 主密钥加密(3DES-ECB)得到sDesKey = "498C189C17F5E37750B55FE757865000";

 */

public class ActivitySimple extends Activity {

    public static int currMasterKeyIndex;
    public static int currMasterKeyLeft;
    public static int currMasterKeyRight;
    public static int currPinKeyIndex;
    public static int currDesKeyIndex;
    public static int currMacKeyIndex;
    Context mContext = ActivitySimple.this;

    private  final int MSG_SHOW_PINBLOCK = 1;
    private  final int MSG_SHOW_FAIL = 2;

    //pin参数
    PinParam pinParam;

    String sMasterKey;
    String sPinKey;
    String sDesKey;
    Button btn_masterkey;
    Button btn_pin_amount_cardno;
    Button btn_pin_amount;
    Button btn_pin_cardno;
    Button btn_writedeskey;
    Button btn_des;
    Button btn_writepinkey;
    Button bn_dukpt_getpin;
    Button bn_wrt_bdk;
    EditText edt_pinblock;
    EditText edt_pindes;

    Button btn_1;
    Button btn_2;
    Button btn_3;
    Handler handler;

    TextView title_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("PINPAD EXAMPLE");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_FAIL){
                    String sErrorCode = (String)msg.obj;
                    Toast.makeText(ActivitySimple.this,"FAIL! ERROR CODE " + sErrorCode , Toast.LENGTH_SHORT).show();
                }else {
                    edt_pinblock.setText((String)msg.obj);
                }
            }
        };
//        sMasterKey = "30313233343536373839414243444546";  //主密钥
//        sPinKey = "50B55FE757865000498C189C17F5E377";     //用主密钥加密后的pin密钥
//        sDesKey = "498C189C17F5E37750B55FE757865000";     //用主密钥加密后的des加密密钥

        sMasterKey = "55DD21B40C3CB4F6CFC393A960123CE8";  //主密钥
        sPinKey = "68C888ED19B82501BBD7F4A1EFD1AFF2";     //用主密钥加密后的pin密钥
        sDesKey = "498C189C17F5E37750B55FE757865000";     //用主密钥加密后的des加密密钥

        currMasterKeyIndex = 0;
        currMasterKeyLeft = 1;
        currMasterKeyRight = 2;
        currPinKeyIndex = 3;
        currDesKeyIndex = 4;
        currMacKeyIndex = 5;
        int i;
        i = EmvService.Open(ActivitySimple.this); //返回1成功， 其他失败
        Log.d("telpo", "EMVservice open:" + i);
        if(i != 1){
            Toast.makeText(ActivitySimple.this,"EMVservice open fail", Toast.LENGTH_SHORT).show();
        }

        i = EmvService.deviceOpen();//返回0成功，其他失败
        Log.d("telpo", "EMVservice deviceOpen open:" + i);
        if(i != 0){
            Toast.makeText(ActivitySimple.this," EmvService.deviceOpen fail", Toast.LENGTH_SHORT).show();
        }

        i = PinpadService.Open((ActivitySimple.this));//返回0成功其他失败
        Log.d("telpo", "PinpadService deviceOpen open:" + i);

        if (i == PinpadService.PIN_ERROR_NEED_TO_FOMRAT){
            PinpadService.TP_PinpadFormat(mContext);
            i = PinpadService.Open((ActivitySimple.this));//返回0成功其他失败
        }
        Log.d("telpo", "PinpadService deviceOpen open:" + i);
        if(i != 0){
            Toast.makeText(ActivitySimple.this,"PinpadService open faol", Toast.LENGTH_SHORT).show();
        }

        edt_pindes = (EditText)findViewById(R.id.edt_pindes);
        edt_pinblock = (EditText)findViewById(R.id.edt_pinblock);

        //写密钥加密密钥
        btn_masterkey = (Button) findViewById(R.id.btn_masterkey);
        btn_masterkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个参数为密钥索引，用加密函数时指定加密密钥
                //第二个参数为密钥
                //第三个参数为模式KEY_WRITE_DIRECT：
                //第四个参数为 给第三个参数加密的密钥
                int i = PinpadService.TP_WriteMasterKey(currMasterKeyIndex,hexStringToByte(sMasterKey), PinpadService.KEY_WRITE_DIRECT);

                Log.d("telpo", "TP_WritePinKey:" + i);
                if (i == 0){
                    Toast.makeText(ActivitySimple.this,"success!", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(ActivitySimple.this,"FAIL!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


        //写加密密钥
        btn_writedeskey = (Button) findViewById(R.id.btn_writedeskey);
        btn_writedeskey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个参数为密钥索引，用加密函数时指定加密密钥
                //第二个参数为密钥
                //第三个参数为模式KEY_WRITE_DIRECT：直接写进去  KEY_WRITE_DECRYPT：先用第四个参数所指定的密钥解密第三个参数所代表加密的密钥，再写进去
                //第四个参数为 给第三个参数加密的密钥
                int i = PinpadService.TP_WriteDesKey(currDesKeyIndex,hexStringToByte(sDesKey), PinpadService.KEY_WRITE_DECRYPT, currMasterKeyIndex);
                if (i == 0){
                    Toast.makeText(ActivitySimple.this,"success!", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(ActivitySimple.this,"FAIL!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("telpo", "TP_WritePinKey:" + i);



            }
        });

        //写pin密钥
        btn_writepinkey = (Button) findViewById(R.id.btn_writepinkey);
        btn_writepinkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个参数为密钥索引，用加密函数时指定加密密钥
                //第二个参数为密钥
                //第三个参数为模式KEY_WRITE_DIRECT：直接写进去 直接写进去  KEY_WRITE_DECRYPT：先用第四个参数所指定的密钥解密第三个参数所代表加密的密钥，再写进去。
                //第四个参数为 给第三个参数加密的密钥
                int i = PinpadService.TP_WritePinKey(currPinKeyIndex, hexStringToByte(sPinKey), PinpadService.KEY_WRITE_DECRYPT, currMasterKeyIndex);
                Log.d("telpo", "TP_WritePinKey:" + i);
                if (i == 0){
                    Toast.makeText(ActivitySimple.this,"success!", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(ActivitySimple.this,"FAIL!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


       // EmvService.Poweroff_Resume();

        //加密数据
        btn_des = (Button) findViewById(R.id.btn_des);
        btn_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] value = {0x00, 0x00, 0x00, 0x00, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33};
                int len = value.length/8*8 + (value.length%8==0? 0:8);  //加密后的数据长度是8的倍数，如果value长度是8的倍数则与value一样长，如果不是8的倍数就则是比value大的第一个8的倍数。
                byte[] encryptBlock = new byte[len];
                //第一个参数为密钥索引，用加密函数时指定加密密钥
                //第二个参数为要加密的数据
                //第三个参数为模式加密后的数据
                int i = PinpadService.TP_DesByKeyIndex(currDesKeyIndex, value, encryptBlock, PinpadService.PIN_DES_ENCRYPT );

                if (i == PinpadService.PIN_OK){
                    edt_pindes.setText(bytesToHexString_upcase(encryptBlock));
                }else {
                    Toast.makeText(ActivitySimple.this,"FAIL! ERROR CODE " + i, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //调用密码键盘 有金额和卡号
        //例子：
        //密码：1234 卡号：4838340177005006 加密密钥：32323232323232323131313131313131 （前面已经写入）
        // 则PINBlock：0412B7BFE88FFAFF
        //加密结果：8C54067D0F21CF25
        btn_pin_amount_cardno = (Button) findViewById(R.id.btn_pin_amount_cardno);
        btn_pin_amount_cardno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinParam = new PinParam(ActivitySimple.this);
                pinParam.CardNo = "4838340177005006";   //银行卡号
                pinParam.IsShowCardNo = 1;  //IsShowCardNo为1表示显示银行卡卡号，否则不现实
                pinParam.Amount = "100.00"; //pinParam.Amount有赋值则显示金额，否则不显示
                pinParam.KeyIndex = currPinKeyIndex;    //密钥索引
                pinParam.MaxPinLen = 4;                 //密码最大长度
                pinParam.MinPinLen = 4;                 //密码最小长度
                pinParam.WaitSec = 1000;                  //密码键盘超时时间
                //最后加密结果在pinParam.Pin_Block中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //返回PinpadService.PIN_OK成功，其他失败
                        int i = PinpadService.TP_PinpadGetPin(pinParam);
                        Log.d("FanZ", "TP_PinpadGetPin：" + i);
                        if (i == PinpadService.PIN_OK){
                            Message m = new Message();
                            m.what = MSG_SHOW_PINBLOCK;
                            //获取结果
                            m.obj = StringUtil.bytesToHexString_upcase(pinParam.Pin_Block);
                            handler.sendMessage(m);
                        }
                        else
                        {
                            Message m = new Message();
                            m.what = MSG_SHOW_FAIL;
                            m.obj = ""+i;
                            handler.sendMessage(m);
                        }
                        Log.d("FanZ", "run: " + bytesToHexString_upcase(pinParam.Pin_Block));

                    }
                }).start();
           }
        });

        //调用密码键盘 有金额
        btn_pin_amount = (Button) findViewById(R.id.btn_pin_amount);
        btn_pin_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinParam = new PinParam(ActivitySimple.this);
                pinParam.IsShowCardNo = 0;
                pinParam.CardNo = "4838340177005006";   //银行卡号
                pinParam.Amount = "100.00";
                pinParam.KeyIndex = currPinKeyIndex;    //密钥索引
                pinParam.MaxPinLen = 4;                 //密码最大长度
                pinParam.MinPinLen = 4;                 //密码最小长度
                pinParam.WaitSec = 20;                  //密码键盘超时时间
                //最后加密结果在pinParam.Pin_Block中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //返回PinpadService.PIN_OK成功，其他失败
                        int i = PinpadService.TP_PinpadGetPin(pinParam);
                        Log.d("FanZ", "TP_PinpadGetPin：" + i);
                        if (i == PinpadService.PIN_OK){
                            Message m = new Message();
                            m.what = MSG_SHOW_PINBLOCK;
                            //获取结果
                            m.obj = StringUtil.bytesToHexString_upcase(pinParam.Pin_Block);
                            handler.sendMessage(m);
                        }
                        else
                        {
                            Message m = new Message();
                            m.what = MSG_SHOW_FAIL;
                            m.obj = ""+i;
                            handler.sendMessage(m);
                        }
                        Log.d("FanZ", "run: " + bytesToHexString_upcase(pinParam.Pin_Block));

                    }
                }).start();
            }
        });


        //调用密码键盘 卡号
        btn_pin_cardno = (Button) findViewById(R.id.btn_pin_cardno);
        btn_pin_cardno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinParam = new PinParam(ActivitySimple.this);
                pinParam.CardNo = "4838340177005006";   //银行卡号
                pinParam.IsShowCardNo = 1;
                pinParam.KeyIndex = currPinKeyIndex;    //密钥索引
                pinParam.MaxPinLen = 4;                 //密码最大长度
                pinParam.MinPinLen = 4;                 //密码最小长度
                pinParam.WaitSec = 20;                  //密码键盘超时时间
                //最后加密结果在pinParam.Pin_Block中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //返回PinpadService.PIN_OK成功，其他失败
                        int i = PinpadService.TP_PinpadGetPin(pinParam);
                        Log.d("FanZ", "TP_PinpadGetPin：" + i);
                        if (i == PinpadService.PIN_OK){
                            Message m = new Message();
                            m.what = MSG_SHOW_PINBLOCK;
                            //获取结果
                            m.obj = StringUtil.bytesToHexString_upcase(pinParam.Pin_Block);
                            handler.sendMessage(m);
                        }
                        else
                        {
                            Message m = new Message();
                            m.what = MSG_SHOW_FAIL;
                            m.obj = ""+i;
                            handler.sendMessage(m);
                        }
                        Log.d("FanZ", "run: " + bytesToHexString_upcase(pinParam.Pin_Block));

                    }
                }).start();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int i = EmvService.deviceClose();
        Log.d("telpo", "Device close: " + i);
        PinpadService.Close();
        Log.d("telpo", "Device close");
    }
    public static byte[] hexStringToByte(String hex) {
        if(hex == null || hex.length()==0){
            return null;
        }
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
    public static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static String bytesToHexString_upcase(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");

        if (src == null || src.length <= 0) {
            return "";
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length ; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().toUpperCase();
    }

}

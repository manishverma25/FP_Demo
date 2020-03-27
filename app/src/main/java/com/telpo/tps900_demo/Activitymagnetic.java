package com.telpo.tps900_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.PaypassErrorData;
import com.telpo.emv.PaypassOutCome;
import com.telpo.emv.PaypassParam;
import com.telpo.emv.PaypassResult;
import com.telpo.emv.PaypassUserData;
import com.telpo.emv.QvsdcParam;
import com.telpo.emv.util.StringUtil;
import com.telpo.pinpad.PinParam;
import com.telpo.pinpad.PinpadService;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.telpo.tps900_demo.dialog.DialogListener;
import com.telpo.tps900_demo.dialog.TelpoProgressDialog;
import com.telpo.tps900_demo.dialog.WritePadDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.telpo.util.StringUtil.hexStringToByte;

public class Activitymagnetic extends Activity {
    TextView title_tv;
    EditText inputAmt;
    Button sale;

    TelpoProgressDialog progressDialog = null;
    public static int Mag = 0;
    public static int IC = 1;
    public static int Nfc = 2;

    EmvService emvService;
    Context context;
    String[] data = new String[3];
    String cardNum;
    String Amount;

    int event;
    int ret;

    MediaPlayer OKplayer;
    MediaPlayer FAILplayer;
    MediaPlayer notionPlayer;
    MediaPlayer stopPlayer;
    MediaPlayer rejectPlayer;

    boolean userCancel = false;

    Handler handler;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;
    KeyguardManager km;
    String sMasterKey,sMasterKey1;
    String sPinKey,sPinKey1;
    String sDesKey;
    WritePadDialog writePadDialog;
    UsbThermalPrinter usbThermalPrinter=new UsbThermalPrinter(Activitymagnetic.this);
    boolean waitsign=true;
    Bitmap bitmap;
    TelpoProgressDialog.OnTimeOutListener dialogTimeOutListener = new TelpoProgressDialog.OnTimeOutListener() {
        @Override
        public void onTimeOut() {

            new AlertDialog.Builder(context)
                    .setTitle(R.string.overTime)
                    .setPositiveButton(R.string.bn_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("EMV FOR MAGNETIC");
        context=Activitymagnetic.this;


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case 1:
                        progressDialog.setTitle(getString(R.string.server_connecting));//
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setMessage(getString(R.string.please_wait));
                        progressDialog.show();
                        break;
                    case 2:
                        writePadDialog.setCancelable(true);
                        writePadDialog.setCanceledOnTouchOutside(false);
                        writePadDialog.show();
                        break;
                    default:break;
                }
            }
        };

        OKplayer = MediaPlayer.create(Activitymagnetic.this, R.raw.success1);
        FAILplayer = MediaPlayer.create(Activitymagnetic.this, R.raw.fail1);
        stopPlayer = MediaPlayer.create(Activitymagnetic.this, R.raw.trans_stop1);
        rejectPlayer = MediaPlayer.create(Activitymagnetic.this, R.raw.trans_reject1);

        writePadDialog=new WritePadDialog(Activitymagnetic.this, new DialogListener() {
            @Override
            public void refreshActivity(Object object) {
                bitmap=(Bitmap)object;
                bitmap= ThumbnailUtils.extractThumbnail(bitmap,360,256);
                waitsign=false;
            }
        });

        progressDialog = new TelpoProgressDialog(context, 80000, dialogTimeOutListener);


        sMasterKey = "55DD21B40C3CB4F6CFC393A960123CE8";  //主密钥
        sMasterKey1="C2923C1C6CFE6D1EFD3CA9ECF68E63AA";

        sPinKey = "68C888ED19B82501BBD7F4A1EFD1AFF2";     //用主密钥加密后的pin密钥
        sPinKey1 = "F0BB4B6F425CE3CDF10BAB787563D328";

        sDesKey = "498C189C17F5E37750B55FE757865000";     //用主密钥加密后的des加密密钥


        ret = EmvService.Open(Activitymagnetic.this);
        if(ret != EmvService.EMV_TRUE){
           Log.e("yw","EmvService.Open fail");
           Toast.makeText(Activitymagnetic.this,"EmvService.Open fail",Toast.LENGTH_SHORT).show();
        }

        ret = EmvService.deviceOpen();
        if( ret != 0){
            Log.e("yw","EmvService.Open fail");
            Toast.makeText(Activitymagnetic.this,"EmvService.Open fail",Toast.LENGTH_SHORT).show();
        }




        ret = PinpadService.Open((Activitymagnetic.this));//返回0成功其他失败


        if (ret == PinpadService.PIN_ERROR_NEED_TO_FOMRAT){
            PinpadService.TP_PinpadFormat(Activitymagnetic.this);
            ret = PinpadService.Open((Activitymagnetic.this));//返回0成功其他失败
        }
        Log.d("telpo", "PinpadService deviceOpen open:" + ret);
        if(ret != 0){
            Toast.makeText(Activitymagnetic.this,"PinpadService open fail", Toast.LENGTH_SHORT).show();
        }


        int i = PinpadService.TP_WriteMasterKey(0,hexStringToByte(sMasterKey1), PinpadService.KEY_WRITE_DIRECT);

        Log.d("yw", "TP_WriteMasterKey:" + i);
        if (i == 0){
            int t = PinpadService.TP_WritePinKey(1, hexStringToByte(sPinKey1), PinpadService.KEY_WRITE_DECRYPT, 0);
            Log.d("yw", "TP_WritePinKey:" + t);
        }



        inputAmt = (EditText) findViewById(R.id.inputAmt);
        inputAmt.setSelection(inputAmt.getText().length());//移动光标至最右边

        sale = (Button) findViewById(R.id.btn_saleStart);
        sale.setEnabled(false);
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amount=inputAmt.getText().toString();
                new TaskReadCard().execute();
                waitsign=true;
            }
        });
        inputAmt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) Activitymagnetic.this
                        .getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, 0);
                v.requestFocus();
                return true;
            }
        });

        inputAmt.addTextChangedListener(new TextWatcher() {
            private boolean isChanged = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanged) {// ----->如果字符未改变则返回
                    return;
                }
                String str = s.toString();
                isChanged = true;
                String cuttedStr = str;
                /* 删除字符串中的dot */
                for (int i = str.length() - 1; i >= 0; i--) {
                    char c = str.charAt(i);
                    if ('.' == c) {
                        cuttedStr = str.substring(0, i) + str.substring(i + 1);
                        break;
                    }
                }
                /* 删除前面多余的0 */
                int NUM = cuttedStr.length();
                int zeroIndex = -1;
                for (int i = 0; i < NUM - 2; i++) {
                    char c = cuttedStr.charAt(i);
                    if (c != '0') {
                        zeroIndex = i;
                        break;
                    } else if (i == NUM - 3) {
                        zeroIndex = i;
                        break;
                    }
                }
                if (zeroIndex != -1) {
                    cuttedStr = cuttedStr.substring(zeroIndex);
                }
                /* 不足3位补0 */
                if (cuttedStr.length() < 3) {
                    cuttedStr = "0" + cuttedStr;
                }
                /* 加上dot，以显示小数点后两位 */
                cuttedStr = cuttedStr.substring(0, cuttedStr.length() - 2)
                        + "." + cuttedStr.substring(cuttedStr.length() - 2);
                inputAmt.setText(cuttedStr);
                inputAmt.setSelection(inputAmt.length());
                isChanged = false;

                if (cuttedStr.equals("0.00")) {
                    sale.setEnabled(false);
                } else {
                    sale.setEnabled(true);
                }
            }

        });
        //延时启动输入金额的软键盘
        new Timer().schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) inputAmt.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(inputAmt, 0);
            }
        }, 300);
    }


    @Override
    protected void onDestroy() {
        PinpadService.Close();
        EmvService.deviceClose();
        super.onDestroy();
    }

    private class TaskReadCard extends AsyncTask<Integer, String, Integer> {

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();

            //dialog = new ProgressDialog(Activitymagnetic.this);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface progressDialog) {
                    EmvService.MagStripeCloseReader();
                    userCancel = true;
                    cancel(true);
                }
            });
            //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //progressDialog.setIndeterminate(false);
            progressDialog.setTitle("Read card");
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("abc");
            progressDialog.show();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                publishProgress("open device..");
                openDevice();
                Thread.sleep(500);

                publishProgress("detect card ..");
                event = detectCard();

                if (event == Mag) {
                    publishProgress("Read mag card ..");
                    data[0] = EmvService.MagStripeReadStripeData(1);
                    data[1] = EmvService.MagStripeReadStripeData(2);
                    data[2] = EmvService.MagStripeReadStripeData(3);
                    progressDialog.dismiss();
                    //处理卡号
                    String data2 = data[1].toUpperCase();
                    int  i= data2.indexOf('=');
                    if (i < 1 || i > 21) {
                        i = data2.indexOf('D');

                    }
                    cardNum = data2.subSequence(0, i).toString();
                    PinParam param = new PinParam(context);
                    param.KeyIndex = 1;
                    param.WaitSec = 200;
                    param.MaxPinLen = 6;
                    param.MinPinLen= 4;
                    param.CardNo = cardNum;
                    param.IsShowCardNo = 1;
                    param.Amount = Amount;
                    wakeUpAndUnlock(context);
                    ret = PinpadService.TP_PinpadGetPin(param);
                    //  pin-block
                    Log.e("yw", "TP_PinpadGetPin: " +ret +"\nPinblock: " +StringUtil.bytesToHexString(param.Pin_Block) );
                    if ( ret == PinpadService.PIN_ERROR_CANCEL){
                        //交易拒绝
                        rejectPlayer.start();
                    }else if ( ret == PinpadService.PIN_OK && StringUtil.bytesToHexString(param.Pin_Block).contains("00000000")){
                        //密码为空  密码错误
                        rejectPlayer.start();
                    }else if ( ret == PinpadService.PIN_OK ){
                      //模拟联网

                        handler.sendMessage(handler.obtainMessage(1));//

                        // 交易成功   验证签名   打印小票
                            Thread.sleep(2000);
                        progressDialog.dismiss();

                        handler.sendMessage(handler.obtainMessage(2));//


                        while (waitsign){
                            Thread.sleep(500);
                        }
                        OKplayer.start();
                        try {
                            usbThermalPrinter.start(1);
                            usbThermalPrinter.reset();
                            usbThermalPrinter.setMonoSpace(true);
                            usbThermalPrinter.setGray(7);
                            usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                            Bitmap bitmap1= BitmapFactory.decodeResource(context.getResources(),R.mipmap.telpoe);
                            Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bitmap1, 244, 116);
                            usbThermalPrinter.printLogo(bitmap2,true);

                            usbThermalPrinter.setTextSize(30);
                            usbThermalPrinter.addString("POS SALES SLIP\n");
                            usbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                            usbThermalPrinter.setTextSize(24);
                            usbThermalPrinter.addString("MERCHANT NAME:             Telpo");
                            usbThermalPrinter.addString("MERCHANT NO:                  01");
                            usbThermalPrinter.addString("TERMINAL NO:                  02");
                             i = usbThermalPrinter.measureText("CARD NO:" + cardNum);
                            int i1 = usbThermalPrinter.measureText(" ");
                            int SpaceNumber=(384-i)/i1;
                            String spaceString = "";
                            for (int j=0;j<SpaceNumber;j++){
                                spaceString+=" ";
                            }

                            usbThermalPrinter.addString("CARD NO:"+spaceString+cardNum);
                            usbThermalPrinter.addString("TRANS TYPE:                GOODS");
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                            String str = formatter.format(curDate);
                            usbThermalPrinter.addString("DATE/TIME:   "+str);
                            usbThermalPrinter.addString("EXP DATE:             2019-12-30" );
                            usbThermalPrinter.addString("BATCH NO:             2019000168");
                            usbThermalPrinter.addString("REFER NO:             2019001232");
                            i = usbThermalPrinter.measureText("AMOUNT:" + "$"+ Amount);
                            i1 = usbThermalPrinter.measureText(" ");
                            SpaceNumber=(384-i)/i1;
                            spaceString = "";
                            for (int j=0;j<SpaceNumber;j++){
                                spaceString+=" ";
                            }
                            usbThermalPrinter.addString("AMOUNT:" + spaceString +"$"+ Amount);
                            usbThermalPrinter.addString("CARD HOLDER SIGNATURE:");
                            usbThermalPrinter.printLogo(bitmap,true);
                            usbThermalPrinter.printString();
                            usbThermalPrinter.walkPaper(10);
                        } catch (TelpoException e) {
                            e.printStackTrace();
                        }finally {
                            usbThermalPrinter.stop();
                        }

                        //打印
                    }else {
                       //交易失败  查看ret值
                        FAILplayer.start();
                    }


                }


            } catch(InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            finish();
            super.onPostExecute(integer);
        }
    }

    int detectCard() {
        int ret;
        while (true) {
            if (userCancel) {
                return Mag;
            }

                ret = EmvService.MagStripeCheckCard(1000);

                if (ret == 0) {
                    return Mag;
                }
            }


        }


    void openDevice() {
        int ret;
            ret = EmvService.MagStripeOpenReader();
    }

    void deviceClose() {
        int ret;
            ret = EmvService.MagStripeCloseReader();


    }

    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }
}



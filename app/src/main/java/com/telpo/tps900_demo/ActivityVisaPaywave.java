package com.telpo.tps900_demo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.telpo.emv.EmvAmountData;
import com.telpo.emv.EmvCandidateApp;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvPinData;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvServiceListener;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.util.StringUtil;
import com.telpo.pinpad.PinParam;

import java.io.UnsupportedEncodingException;

public class ActivityVisaPaywave extends Activity {

    Context context;
    EmvService emvService;
    Button bn_emvDeviceOpen,bn_emvDeviceClose;
    Button bn_AddAid,bn_AddCapk,bn_readCard,bn_AddCapkTest,
            bn_ref;

    Button[] buttons ;
    TextView title_tv;

    EditText et_reslut;



    StringBuffer logBuf = new StringBuffer("");

    EditText et_bdk,et_ksn,et_EC,
            et_SNkey,et_SN,et_random;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;
    KeyguardManager km;

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if(Intent.ACTION_SCREEN_ON.equals(action)){
                Log.d("lyj", "-----------------screen is on...");
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
                Log.d("lyj", "----------------- screen is off...");
                //wakeLock.acquire();
                Log.d("lyj", "acquire ?");
                //wakeScreen(EmvActivity.this);
                Log.d("lyj", "wakeScreen ?");

                //wakeUpAndUnlock(context);
                Log.d("lyj", "wakeUpAndUnlock ?");
            }
        }

    };

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paywava);
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("Paywave");
        context = ActivityVisaPaywave.this;
        ActivityInit();
        viewInit();
        setButtonsEnable(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        unregisterReceiver(mBatInfoReceiver);


    }

    /**
     * 唤醒屏幕
     */
    public void wakeScreen(Activity activity){
        //屏幕解锁
        km= (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard();

        //屏幕唤醒
        if(wakeLock==null) {
            pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        }
        wakeLock.acquire();
        wakeLock.release();
    }

    private void viewInit(){
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
        wakeLock.acquire();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBatInfoReceiver, filter);


        et_reslut = (EditText) findViewById(R.id.et_reslut);

        bn_emvDeviceOpen = (Button) findViewById(R.id.bn_emvDeviceOpen);
        bn_emvDeviceClose = (Button) findViewById(R.id.bn_emvDeviceClose);
        bn_AddAid = (Button) findViewById(R.id.bn_AddAid);
        bn_AddCapk = (Button) findViewById(R.id.bn_AddCapk);
        bn_AddCapkTest = (Button) findViewById(R.id.bn_AddCapkTest);

        bn_readCard = (Button) findViewById(R.id.bn_readCard);
        bn_ref = (Button) findViewById(R.id.bn_ref);


        buttons = new Button[]{bn_AddAid,bn_AddCapkTest,bn_AddCapk,bn_readCard};

        bn_emvDeviceOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret;
                //ClearDis();
                ret = EmvService.Open(context);
                if(ret != EmvService.EMV_TRUE){
                    AppendDis("Emv open failed ! "+ ret);
                    return;
                }
                AppendDis("Emv open success !");
                ret = EmvService.deviceOpen();
                if( ret != 0){
                    AppendDis("device open failed ! "+ ret);
                    return;
                }

                int i = EmvService.NfcOpenReader(1000);
                AppendDis("Open NFC : " + i);

                AppendDis("device open success !");
                setButtonsEnable(true);

            }
        });

        bn_emvDeviceClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = EmvService.deviceClose();
                if( ret != 0){
                    AppendDis("device close failed !");
                    return;
                }
                AppendDis("device close success !");
                setButtonsEnable(false);
            }
        });

        bn_AddAid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmvService.Emv_RemoveAllApp();
                AppendDis("remove all app !");
                DefaultAPPCAPK.Add_All_APP();
                AppendDis("add all app !");
            }
        });

        bn_AddCapkTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmvService.Emv_RemoveAllCapk();
                AppendDis("remove all capk !");
                DefaultAPPCAPK.Add_All_CAPK_Test();
                AppendDis("add all capk !");
            }
        });

        bn_AddCapk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmvService.Emv_RemoveAllCapk();
                AppendDis("remove all capk !");
                DefaultAPPCAPK.Add_All_CAPK();
                AppendDis("add all capk !");
            }
        });

        bn_readCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskReadVisaPayvaveCard(context, emvService).execute();
            }
        });



        bn_ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearDis();
            }
        });
    }

    private void ActivityInit(){
        EmvService.Emv_SetDebugOn(1);
        emvService = EmvService.getInstance();
        emvService.setListener(listener);
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
                et_reslut.setText(logBuf.toString());
                et_reslut.setSelection(et_reslut.getText().length());
            }
        });
    }

    private void setButtonsEnable(boolean flag){
        if(flag){
            bn_emvDeviceOpen.setEnabled(false);
            bn_emvDeviceClose.setEnabled(true);
            for(Button i:buttons){
                i.setEnabled(flag);
            }
        }else{
            bn_emvDeviceOpen.setEnabled(true);
            bn_emvDeviceClose.setEnabled(false);
            for(Button i:buttons){
                i.setEnabled(flag);
            }
        }
    }



    int mResult;
    boolean bUIThreadisRunning = true;
    boolean bUserCancelInputPIN = true;
    EmvServiceListener listener = new EmvServiceListener() {
        @Override
        public int onInputAmount(EmvAmountData AmountData) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onInputPin(EmvPinData PinData) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onSelectApp(EmvCandidateApp[] appList) {
            return appList[0].index;
        }

        @Override
        public int onSelectAppFail(int ErrCode) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onFinishReadAppData() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onVerifyCert() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onOnlineProcess(EmvOnlineData OnlineData) {

            try {
                OnlineData.ResponeCode = "00".getBytes("ascii");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            final PinParam param = new PinParam(context);
            final int ret;
            EmvTLV pan = new EmvTLV(0x5A);
            ret = emvService.Emv_GetTLV(pan);
            if(ret == EmvService.EMV_TRUE){
                StringBuffer p = new StringBuffer(StringUtil.bytesToHexString(pan.Value));
                if (p.charAt(p.toString().length()-1) == 'F'){
                    p.deleteCharAt(p.toString().length()-1);
                }
                param.CardNo = p.toString();
                Log.w("listener", "CardNo: " + param.CardNo);
            }else{
                pan = new EmvTLV(0x57);
                if(emvService.Emv_GetTLV(pan) == EmvService.EMV_TRUE){
                    String panstr = StringUtil.bytesToHexString(pan.Value);
                    Log.w("pan", "panstr: " + panstr);
                    int index = panstr.indexOf("D");
                    Log.w("pan", "index: " + index);
                    param.CardNo = panstr.substring(0, index);
                }
            }
            AppendDis( "PAN: " + param.CardNo );


            return EmvService.EMV_TRUE;
        }

        @Override
        public int onRequireTagValue(int tag, int len, byte[] value) {
            Log.w("emvlistener", "onRequireTagValue: " + tag);
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onRequireDatetime(byte[] datetime) {

            return EmvService.EMV_TRUE;

        }

        @Override
        public int onReferProc() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int OnCheckException(String PAN) {
            return EmvService.EMV_FALSE;
        }

        @Override
        public int OnCheckException_qvsdc(int index, String PAN) {
            return EmvService.EMV_FALSE;
        }
    };



}

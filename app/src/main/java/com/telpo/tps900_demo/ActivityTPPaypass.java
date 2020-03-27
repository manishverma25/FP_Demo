package com.telpo.tps900_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.telpo.emv.EmvAmountData;
import com.telpo.emv.EmvApp;
import com.telpo.emv.EmvCAPK;
import com.telpo.emv.EmvCandidateApp;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvPinData;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvServiceListener;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.PaypassErrorData;
import com.telpo.emv.PaypassOutCome;
import com.telpo.emv.PaypassParam;
import com.telpo.emv.PaypassResult;
import com.telpo.emv.PaypassUserData;
import com.telpo.emv.util.StringUtil;
import com.telpo.pinpad.PinParam;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityTPPaypass extends Activity {

    Context context;
    EmvService emvService;

    Button bn_emvDeviceOpen, bn_emvDeviceClose, bn_AddAid, bn_AddCapk , bn_AddCapkTest, bn_readCard , bn_set;

    Button[] buttons;
    TextView title_tv;
    EditText et_reslut;
    StringBuffer logBuf = new StringBuffer("");

    ProgressDialog dialog = null;
    boolean userCancel = false;
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
            Log.w("input pin", "onInputPin: " + "callback [onInputPIN]:" + PinData.type);

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
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onRequireTagValue(int tag, int len, byte[] value) {
            Log.w("emvlistener", "onRequireTagValue: " + tag);
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
        public int onRequireDatetime(byte[] datetime) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = formatter.format(curDate);
            byte[] time = new byte[0];
            try {
                time = str.getBytes("ascii");
                System.arraycopy(time, 0, datetime, 0, datetime.length);
                return EmvService.EMV_TRUE;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("MyEmvService", "onRequireDatetime failed");
                return EmvService.EMV_FALSE;
            }
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
            return EmvService.EMV_TRUE;
        }
    };
    public static PaypassParam m_PaypassParam = new PaypassParam();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tppaypass);
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("Paypass");
        context = ActivityTPPaypass.this;
        viewInit();
        ActivityInit();
    }

    private void ActivityInit() {
        EmvService.Emv_SetDebugOn(1);
        emvService = EmvService.getInstance();
        emvService.setListener(listener);
    }

    void viewInit() {
        bn_emvDeviceOpen = (Button) findViewById(R.id.bn_emvDeviceOpen);
        bn_emvDeviceClose = (Button) findViewById(R.id.bn_emvDeviceClose);
        bn_AddAid = (Button) findViewById(R.id.bn_AddAid);
        bn_AddCapk = (Button) findViewById(R.id.bn_AddCapk);
        bn_AddCapkTest = (Button) findViewById(R.id.bn_AddCapkTest);
        bn_readCard = (Button) findViewById(R.id.bn_readCard);
        bn_set = (Button) findViewById(R.id.bn_set);
        buttons = new Button[]{bn_AddAid, bn_AddCapk , bn_AddCapkTest, bn_readCard};

        et_reslut = (EditText) findViewById(R.id.et_reslut);

        bn_emvDeviceOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret;
                //ClearDis();
                ret = EmvService.Open(context);
                if (ret != EmvService.EMV_TRUE) {
                    AppendDis("Emv open failed ! " + ret);
                    return;
                }
                AppendDis("Emv open success !");
                ret = EmvService.deviceOpen();
                if (ret != 0) {
                    AppendDis("device open failed ! " + ret);
                    return;
                }
                AppendDis("device open success !");
                setButtonsEnable(true);
            }
        });

        bn_emvDeviceClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = EmvService.deviceClose();
                if (ret != 0) {
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
                addPaypassAid();
            }
        });

        bn_AddCapkTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaypassCAPKTest();
            }
        });

        bn_AddCapk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaypassCAPK();
            }
        });

        bn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityTPPaypass.this, ActivityPaypassParam.class));
            }
        });
        bn_readCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int ret;
                        AppendDis("========================");
                        AppendDis("try to detect master card");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });

                        int i = EmvService.NfcOpenReader(1000);
                        AppendDis("Open NFC : " + i);
                        if (i != 0) {
                            return;
                        }

                        ret = detectNFC();
                        if (ret == -4) {
                            AppendDis("user cancel");
                        } else if (ret == -1003) {
                            AppendDis("timeout");
                        } else if (ret == 0) {
                            paypass_process_demo();
                        } else {
                            AppendDis("detect error:" + ret);
                        }
                        EmvService.NfcCloseReader();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                            }
                        });

                    }
                }).start();
            }
        });

        Button bn_cls = (Button) findViewById(R.id.bn_ref);
        bn_cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearDis();
            }
        });

        dialog = new ProgressDialog(context);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                emvService.NfcCloseReader();
                userCancel = true;
            }
        });
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(false);
        dialog.setTitle("detect Master card...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("abc");

        setButtonsEnable(false);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(et_reslut,InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(et_reslut.getWindowToken(), 0); //强制隐藏键盘


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    int detectNFC() {
        userCancel = false;
        long j = System.currentTimeMillis();
        int ret = -1;
        while (System.currentTimeMillis() - j < 20 * 1000) {

            if (userCancel == true) {
                return -4;
            }

            ret = EmvService.NfcCheckCard(100);
            if (ret == 0) {
                return 0;
            }
            j++;
        }
        return ret;
    }

    void paypass_process_demo() {
        int ret;
        PaypassParam paypassParam = m_PaypassParam;
       // Log.d("fanz", "paypass_process_demo: "+ m_PaypassParam.toString());
        ret = emvService.Paypass_TransInit(paypassParam);
        AppendDis("Paypass_TransInit: " + ret);
        if (ret != EmvService.EMV_TRUE) {
            return;
        }

        ret = emvService.Paypass_StartApp(900, 0, 978, 2, 0);
        AppendDis("Paypass_StartApp: " + ret + "(0x" + Integer.toHexString(ret) + ")");

        if (ret == PaypassResult.PAYPASS_STATUS_TRYAGAIN) {
            AppendDis("Pls power off and relay to read card again");
            return;
        }

        PaypassOutCome OutComeData = new PaypassOutCome();
        PaypassUserData userData = new PaypassUserData();
        PaypassErrorData errorData = new PaypassErrorData();
        ret = emvService.Paypass_GetResult(OutComeData, userData, errorData);
        AppendDis("Paypass_GetResult: " + ret);
        Show_Result_OutComeData(OutComeData);
        Show_Result_UserData(userData);
        Show_Result_ErrorData(errorData);

    }

    void Show_Result_OutComeData(PaypassOutCome OutComeData) {
        AppendDis("-------- OutComeData ---------");

        TP_DbgSerialPrn("OutComeData.Alternate:%d [0x%02X]", OutComeData.Alternate, OutComeData.Alternate);
        TP_DbgSerialPrn("OutComeData.OnLineRspData:%d [0x%02X]", OutComeData.OnLineRspData, OutComeData.OnLineRspData);
        TP_DbgSerialPrn("OutComeData.RemovalTimeout:%d [0x%02X]", OutComeData.RemovalTimeout, OutComeData.RemovalTimeout);
        TP_DbgSerialPrn("OutComeData.Request:%d [0x%02X]", OutComeData.Request, OutComeData.Request);
        TP_DbgSerialPrn("OutComeData.Present:%d [0x%02X]", OutComeData.Present, OutComeData.Present);


        if ((OutComeData.Present & 0x80) > 0) {
            AppendDis("PRESENT_OUTCOME");
        }

        if ((OutComeData.Present & 0x40) > 0) {
            AppendDis("PRESENT_RESTART");
        }

        if ((OutComeData.Present & 0x20) > 0) {
            AppendDis("PRESENT_DATA");
        }

        if ((OutComeData.Present & 0x10) > 0) {
            AppendDis("PRESENT_DISCRETIONARY");
        }

        if ((OutComeData.Present & 0x08) > 0) {
            AppendDis("PRESENT_RECEIPT");
        }

        TP_DbgSerialPrn("OutComeData.CVM: 0x%02X", OutComeData.CVM);

        switch (OutComeData.CVM) {
            case PaypassResult.PAYPASS_CVM_NOCVM:
                AppendDis("PAYPASS_CVM_NOCVM");
                break;

            case PaypassResult.PAYPASS_CVM_SIGNATURE:
                AppendDis("PAYPASS_CVM_SIGNATURE");
                break;

            case PaypassResult.PAYPASS_CVM_ONLINEPIN:
                AppendDis("PAYPASS_CVM_ONLINEPIN");
                break;

            case PaypassResult.PAYPASS_CVM_CODEVERIFIED:
                AppendDis("PAYPASS_CVM_CODEVERIFIED");
                break;

            case PaypassResult.PAYPASS_CVM_NONE:
                AppendDis("PAYPASS_CVM_NONE");
                break;

            case PaypassResult.PAYPASS_CVM_REQUIRED:
                AppendDis("PAYPASS_CVM_REQUIRED");
                break;
        }

        TP_DbgSerialPrn("OutComeData.Start: 0x%02X", OutComeData.Start);

        switch (OutComeData.Start) {
            case PaypassResult.PAYPASS_START_A:
                AppendDis("PAYPASS_START_A");
                break;

            case PaypassResult.PAYPASS_START_B:
                AppendDis("PAYPASS_START_B");
                break;

            case PaypassResult.PAYPASS_START_C:
                AppendDis("PAYPASS_START_C");
                break;

            case PaypassResult.PAYPASS_START_D:
                AppendDis("PAYPASS_START_D");
                break;

            case PaypassResult.PAYPASS_START_NONE:
                AppendDis("PAYPASS_START_NONE");
                break;
        }
        TP_DbgSerialPrn("OutComeData.Status:  0x%02X", OutComeData.Status);

        switch (OutComeData.Status) {
            case PaypassResult.PAYPASS_STATUS_APPROVED:
                AppendDis("PAYPASS_STATUS_APPROVED");
                break;

            case PaypassResult.PAYPASS_STATUS_DECLINED:
                AppendDis("PAYPASS_STATUS_DECLINED");
                break;

            case PaypassResult.PAYPASS_STATUS_ONLINE:
                AppendDis("PAYPASS_STATUS_ONLINE");
                break;

            case PaypassResult.PAYPASS_STATUS_ENDAPP:
                AppendDis("PAYPASS_STATUS_ENDAPP");
                break;

            case PaypassResult.PAYPASS_STATUS_SELECTNEXT:
                AppendDis("PAYPASS_STATUS_SELECTNEXT");
                break;

            case PaypassResult.PAYPASS_STATUS_ANOTHER_INTERFACE:
                AppendDis("PAYPASS_STATUS_ANOTHER_INTERFACE");
                break;

            case PaypassResult.PAYPASS_STATUS_TRYAGAIN:
                AppendDis("PAYPASS_STATUS_TRYAGAIN");
                break;

            case PaypassResult.PAYPASS_STATUS_NONE:
                AppendDis("PAYPASS_STATUS_NONE");
                break;
        }
    }

    void Show_Result_UserData(PaypassUserData UserData) {

        AppendDis("-------- UserData ---------");
        TP_DbgSerialPrn("UserData.MessID: 0x%02X", UserData.MessID);

        switch (UserData.MessID) {
            case PaypassResult.PAYPASS_MESS_CARD_READ_OK:
                AppendDis("MESS_CARD_READ_OK");
                break;

            case PaypassResult.PAYPASS_MESS_TRY_AGAIN:
                AppendDis("MESS_TRY_AGAIN");
                break;

            case PaypassResult.PAYPASS_MESS_APPROVED:
                AppendDis("MESS_APPROVED");
                break;

            case PaypassResult.PAYPASS_MESS_APPROVED_SIGN:
                AppendDis("MESS_APPROVED_SIGN");
                break;

            case PaypassResult.PAYPASS_MESS_DECLINED:
                AppendDis("MESS_DECLINED");
                break;

            case PaypassResult.PAYPASS_MESS_OTHER_CARD:
                AppendDis("MESS_OTHER_CARD");
                break;

            case PaypassResult.PAYPASS_MESS_INSERT_CARD:
                AppendDis("MESS_INSERT_CARD");
                break;

            case PaypassResult.PAYPASS_MESS_SEE_PHONE:
                AppendDis("MESS_SEE_PHONE");
                break;

            case PaypassResult.PAYPASS_MESS_AUTHORISING:
                AppendDis("MESS_AUTHORISING");
                break;

            case PaypassResult.PAYPASS_MESS_CLEAR_DISPLAY:
                AppendDis("MESS_CLEAR_DISPLAY");
                break;

            case PaypassResult.PAYPASS_MESS_NONE_NONE:
                AppendDis("MESS_NONE_NONE");
                break;

        }

        //ENUM_PAYUSER_STATUS;
        TP_DbgSerialPrn("UserData.Status:0x%X", UserData.Status);

        switch (UserData.Status) {
            case PaypassResult.PAYUSER_NOT_READY:
                TP_DbgSerialPrn("PAYUSER_NOT_READY");
                break;

            case PaypassResult.PAYUSER_IDLE:
                TP_DbgSerialPrn("PAYUSER_IDLE");
                break;

            case PaypassResult.PAYUSER_READY_READ:
                TP_DbgSerialPrn("PAYUSER_READY_READ");
                break;

            case PaypassResult.PAYUSER_PROCESSING:
                TP_DbgSerialPrn("PAYUSER_PROCESSING");
                break;

            case PaypassResult.PAYUSER_READ_OK:
                TP_DbgSerialPrn("PAYUSER_READ_OK");
                break;

            case PaypassResult.PAYUSER_PROC_ERROR:
                TP_DbgSerialPrn("PAYUSER_PROC_ERROR");
                break;

            case PaypassResult.PAYUSER_NONE_NONE:
                TP_DbgSerialPrn("PAYUSER_NONE_NONE");
                break;

        }

        TP_DbgSerialPrn("UserData.HoldTimeMs:", UserData.HoldTimeMs, 3);
        TP_DbgSerialPrn("UserData.Language:%s", UserData.Language);
        TP_DbgSerialPrn("UserData.Value:", UserData.Value, 6);
        TP_DbgSerialPrn("UserData CurrencyCode:", UserData.CurrencyCode, 2);

        switch (UserData.ValueQualifier) {
            case PaypassResult.PAYPASS_QUALIFIER_NONE:
                AppendDis("UserData.ValueQualifier: NONE");
                break;

            case PaypassResult.PAYPASS_QUALIFIER_AMOUNT:
                AppendDis("UserData.ValueQualifier: Amount");
                break;

            case PaypassResult.PAYPASS_QUALIFIER_BALANCE:
                AppendDis("UserData.ValueQualifier: Balance");
                break;
        }

    }

    void Show_Result_ErrorData(PaypassErrorData ErrorData) {

        String[] L1_Txt =
                {
                        "L1_ERR_OK",
                        "L1_ERR_TIMEOUT",
                        "L1_ERR_TRANSMISSION",
                        "L1_ERR_PROTOCOL",
                };

        String[] L2_Txt =
                {
                        "L2_ERR_OK",
                        "L2_ERR_DATA_MISSING",
                        "L2_ERR_CAM_FAILED",
                        "L2_ERR_STATUS_BYTES",
                        "L2_ERR_PARSING",
                        "L2_ERR_MAX_EXCEED",
                        "L2_ERR_CARD_DATA",
                        "L2_ERR_MAG_NOT_SUPPORT",
                        "L2_ERR_NO_PPSE",
                        "L2_ERR_PPS_EFAULT",
                        "L2_ERR_EMPTY_CANDIDATE_LIST",
                        "L2_ERR_IDS_READ",
                        "L2_ERR_IDS_WRITE",
                        "L2_ERR_IDS_DATA",
                        "L2_ERR_IDS_NOMATCHING_AC",
                        "L2_ERR_TERM_DATA",
                };

        String[] L3_Txt =
                {
                        "L3_ERR_OK",
                        "L3_ERR_TIMEOUT",
                        "L3_ERR_STOP",
                        "L3_ERR_NO_AMOUNT",
                };

        TP_DbgSerialPrn("-------- ErrorData ---------");
        TP_DbgSerialPrn("ErrorData.L1Error:[%d]%s", ErrorData.L1Error, L1_Txt[ErrorData.L1Error]);
        TP_DbgSerialPrn("ErrorData.L2Error:[%d]%s", ErrorData.L2Error, L2_Txt[ErrorData.L2Error]);
        TP_DbgSerialPrn("ErrorData.L3Error:[%d]%s", ErrorData.L3Error, L3_Txt[ErrorData.L3Error]);
        TP_DbgSerialPrn("ErrorData.SW1:0x%02X", ErrorData.SW1, ErrorData.SW1);
        TP_DbgSerialPrn("ErrorData.SW2:0x%02X", ErrorData.SW2, ErrorData.SW2);

    }

    void TP_DbgSerialPrn(String format, Object... args) {
        String ms = String.format(format, args);
        AppendDis(ms);

    }

    private void setButtonsEnable(boolean flag) {
        if (flag) {
            bn_emvDeviceOpen.setEnabled(false);
            bn_emvDeviceClose.setEnabled(true);
            for (Button i : buttons) {
                i.setEnabled(flag);
            }
        } else {
            bn_emvDeviceOpen.setEnabled(true);
            bn_emvDeviceClose.setEnabled(false);
            for (Button i : buttons) {
                i.setEnabled(flag);
            }
        }
    }

    void AppendDis(String Mes) {
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

    void ClearDis() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_reslut.setText("");
                logBuf = new StringBuffer("");
            }
        });
    }

    void addPaypassAid() {
        int result;
        String name;
        EmvApp APP_1 = new EmvApp();
        name = "Mastercard";
        try {
            APP_1.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        APP_1.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04, 0x10, 0x10};
        APP_1.SelFlag = (byte) 0x00;
        APP_1.Priority = (byte) 0x00;
        APP_1.TargetPer = (byte) 20;
        APP_1.MaxTargetPer = (byte) 50;
        APP_1.FloorLimitCheck = (byte) 1;
        APP_1.RandTransSel = (byte) 1;
        APP_1.VelocityCheck = (byte) 1;
        APP_1.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x50, (byte) 0x00};//9F1B:FloorLimit
        APP_1.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00};
        APP_1.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACOnline = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACDefault = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.AcquierId = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x10};
        APP_1.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        APP_1.TDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x02, (byte) 0x06};
        APP_1.Version = new byte[]{(byte) 0x00, (byte) 0x96};
        APP_1.RiskManData = new byte[]{0x6C, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        result = EmvService.Emv_AddApp(APP_1);
        AppendDis("add " + name + " : " + result);


        APP_1 = new EmvApp();
        name = "Maestro";
        try {
            APP_1.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        APP_1.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04, 0x30, 0x60};
        APP_1.SelFlag = (byte) 0x00;
        APP_1.Priority = (byte) 0x00;
        APP_1.TargetPer = (byte) 0;
        APP_1.MaxTargetPer = (byte) 0;
        APP_1.FloorLimitCheck = (byte) 1;
        APP_1.RandTransSel = (byte) 1;
        APP_1.VelocityCheck = (byte) 1;
        APP_1.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x50, (byte) 0x00};//9F1B:FloorLimit
        APP_1.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00};
        APP_1.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACOnline = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACDefault = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.AcquierId = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x10};
        APP_1.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        APP_1.TDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x02, (byte) 0x06};
        APP_1.Version = new byte[]{(byte) 0x00, (byte) 0x96};
        APP_1.RiskManData = new byte[]{0x44, (byte) 0xFF, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00};
        result = EmvService.Emv_AddApp(APP_1);
        AppendDis("add " + name + " : " + result);

        APP_1 = new EmvApp();
        name = "Test1";
        try {
            APP_1.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        APP_1.AID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        APP_1.SelFlag = (byte) 0x00;
        APP_1.Priority = (byte) 0x00;
        APP_1.TargetPer = (byte) 0;
        APP_1.MaxTargetPer = (byte) 0;
        APP_1.FloorLimitCheck = (byte) 1;
        APP_1.RandTransSel = (byte) 1;
        APP_1.VelocityCheck = (byte) 1;
        APP_1.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x50, (byte) 0x00};//9F1B:FloorLimit
        APP_1.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00};
        APP_1.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACOnline = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.TACDefault = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        APP_1.AcquierId = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x10};
        APP_1.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        APP_1.TDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x02, (byte) 0x06};
        APP_1.Version = new byte[]{(byte) 0x00, (byte) 0x96};
        APP_1.RiskManData = new byte[]{0x6C, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        result = EmvService.Emv_AddApp(APP_1);
        AppendDis("add " + name + " : " + result);
    }

    void addPaypassCAPKTest() {
        int result;
        EmvCAPK capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x00;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = new byte[]{
                (byte) 0x9C, 0x6B, (byte) 0xE5, (byte) 0xAD, (byte) 0xB1, 0x0B, 0x4B, (byte) 0xE3, (byte) 0xDC, (byte) 0xE2, 0x09,
                (byte) 0x9B, 0x4B, 0x21, 0x06, 0x72, (byte) 0xB8, (byte) 0x96, 0x56, (byte) 0xEB, (byte) 0xA0, (byte) 0x91,
                0x20, 0x4F, 0x61, 0x3E, (byte) 0xCC, 0x62, 0x3B, (byte) 0xED, (byte) 0xC9, (byte) 0xC6, (byte) 0xD7,
                0x7B, 0x66, 0x0E, (byte) 0x8B, (byte) 0xAE, (byte) 0xEA, 0x7F, 0x7C, (byte) 0xE3, 0x0F, 0x1B,
                0x15, 0x38, 0x79, (byte) 0xA4, (byte) 0xE3, 0x64, 0x59, 0x34, 0x3D, 0x1F, (byte) 0xE4,
                0x7A, (byte) 0xCD, (byte) 0xBD, 0x41, (byte) 0xFC, (byte) 0xD7, 0x10, 0x03, 0x0C, 0x2B, (byte) 0xA1,
                (byte) 0xD9, 0x46, 0x15, (byte) 0x97, (byte) 0x98, 0x2C, 0x6E, 0x1B, (byte) 0xDD, 0x08, 0x55,
                0x4B, 0x72, 0x6F, 0x5E, (byte) 0xFF, 0x79, 0x13, (byte) 0xCE, 0x59, (byte) 0xE7, (byte) 0x9E,
                0x35, 0x72, (byte) 0x95, (byte) 0xC3, 0x21, (byte) 0xE2, 0x6D, 0x0B, (byte) 0x8B, (byte) 0xE2, 0x70,
                (byte) 0xA9, 0x44, 0x23, 0x45, (byte) 0xC7, 0x53, (byte) 0xE2, (byte) 0xAA, 0x2A, (byte) 0xCF, (byte) 0xC9,
                (byte) 0xD3, 0x08, 0x50, 0x60, 0x2F, (byte) 0xE6, (byte) 0xCA, (byte) 0xC0, 0x0C, 0x6D, (byte) 0xDF,
                0x6B, (byte) 0x8D, (byte) 0x9D, (byte) 0x9B, 0x48, 0x79, (byte) 0xB2, (byte) 0x82, 0x6B, 0x04, 0x2A,
                0x07, (byte) 0xF0, (byte) 0xE5, (byte) 0xAE, 0x52, 0x6A, 0x3D, 0x3C, 0x4D, 0x22, (byte) 0xC7,
                0x2B, (byte) 0x9E, (byte) 0xAA, 0x52, (byte) 0xEE, (byte) 0xD8, (byte) 0x89, 0x38, 0x66, (byte) 0xF8, 0x66,
                0x38, 0x7A, (byte) 0xC0, 0x5A, 0x13, (byte) 0x99
        };
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = new byte[]{(byte) 0xEC, 0x0A, 0x59, (byte) 0xD3, 0x5D, 0x19, (byte) 0xF0, 0x31, (byte) 0xE9, (byte) 0xE8, (byte) 0xCB, (byte) 0xEC, 0x56, (byte) 0xDB, (byte) 0x80, (byte) 0xE2, 0x2B, 0x1D, (byte) 0xE1, 0x30};
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x05;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = new byte[]{
//                (byte) 0xA1, (byte) 0xF5, (byte) 0xE1, (byte) 0xC9, (byte) 0xBD, (byte) 0x86, 0x50, (byte) 0xBD, 0x43, (byte) 0xAB, 0x6E,
//                (byte) 0xE5, 0x6B, (byte) 0x89, 0x1E, (byte) 0xF7, 0x45, (byte) 0x9C, 0x0A, 0x24, (byte) 0xFA, (byte) 0x84,
//                (byte) 0xF9, 0x12, 0x7D, 0x1A, 0x6C, 0x79, (byte) 0xD4, (byte) 0x93, 0x0F, 0x6D, (byte) 0xB1,
//                (byte) 0x85, 0x2E, 0x25, 0x10, (byte) 0xF1, (byte) 0x8B, 0x61, (byte) 0xCD, 0x35, 0x4D, (byte) 0xB8,
//                0x3A, 0x35, 0x6B, (byte) 0xD1, (byte) 0x90, (byte) 0xB8, (byte) 0x8A, (byte) 0xB8, (byte) 0xDF, 0x04, 0x28,
//                0x4D, 0x02, (byte) 0xA4, 0x20, 0x4A, 0x7B, 0x6C, (byte) 0xB7, (byte) 0xC5, 0x55, 0x19,
//                0x77, (byte) 0xA9, (byte) 0xB3, 0x63, 0x79, (byte) 0xCA, 0x3D, (byte) 0xE1, (byte) 0xA0, (byte) 0x8E, 0x69,
//                (byte) 0xF3, 0x01, (byte) 0xC9, 0x5C, (byte) 0xC1, (byte) 0xC2, 0x05, 0x06, (byte) 0x95, (byte) 0x92, 0x75,
//                (byte) 0xF4, 0x17, 0x23, (byte) 0xDD, 0x5D, 0x29, 0x25, 0x29, 0x05, 0x79, (byte) 0xE5,
//                (byte) 0xA9, 0x5B, 0x0D, (byte) 0xF6, 0x32, 0x3F, (byte) 0xC8, (byte) 0xE9, 0x27, 0x3D, 0x6F,
//                (byte) 0x84, (byte) 0x91, (byte) 0x98, (byte) 0xC4, (byte) 0x99, 0x62, 0x09, 0x16, 0x6D, (byte) 0x9B, (byte) 0xFC,
//                (byte) 0x97, 0x3C, 0x36, 0x1C, (byte) 0xC8, 0x26, (byte) 0xE1

                (byte) 0xB8, (byte) 0x04, (byte) 0x8A, (byte) 0xBC, (byte) 0x30, (byte) 0xC9, (byte) 0x0D, (byte) 0x97, (byte) 0x63, (byte) 0x36, (byte) 0x54, (byte) 0x3E, (byte) 0x3F, (byte) 0xD7, (byte) 0x09, (byte) 0x1C,
                (byte) 0x8F, (byte) 0xE4, (byte) 0x80, (byte) 0x0D, (byte) 0xF8, (byte) 0x20, (byte) 0xED, (byte) 0x55, (byte) 0xE7, (byte) 0xE9, (byte) 0x48, (byte) 0x13, (byte) 0xED, (byte) 0x00, (byte) 0x55, (byte) 0x5B,
                (byte) 0x57, (byte) 0x3F, (byte) 0xEC, (byte) 0xA3, (byte) 0xD8, (byte) 0x4A, (byte) 0xF6, (byte) 0x13, (byte) 0x1A, (byte) 0x65, (byte) 0x1D, (byte) 0x66, (byte) 0xCF, (byte) 0xF4, (byte) 0x28, (byte) 0x4F,
                (byte) 0xB1, (byte) 0x3B, (byte) 0x63, (byte) 0x5E, (byte) 0xDD, (byte) 0x0E, (byte) 0xE4, (byte) 0x01, (byte) 0x76, (byte) 0xD8, (byte) 0xBF, (byte) 0x04, (byte) 0xB7, (byte) 0xFD, (byte) 0x1C, (byte) 0x7B,
                (byte) 0xAC, (byte) 0xF9, (byte) 0xAC, (byte) 0x73, (byte) 0x27, (byte) 0xDF, (byte) 0xAA, (byte) 0x8A, (byte) 0xA7, (byte) 0x2D, (byte) 0x10, (byte) 0xDB, (byte) 0x3B, (byte) 0x8E, (byte) 0x70, (byte) 0xB2,
                (byte) 0xDD, (byte) 0xD8, (byte) 0x11, (byte) 0xCB, (byte) 0x41, (byte) 0x96, (byte) 0x52, (byte) 0x5E, (byte) 0xA3, (byte) 0x86, (byte) 0xAC, (byte) 0xC3, (byte) 0x3C, (byte) 0x0D, (byte) 0x9D, (byte) 0x45,
                (byte) 0x75, (byte) 0x91, (byte) 0x64, (byte) 0x69, (byte) 0xC4, (byte) 0xE4, (byte) 0xF5, (byte) 0x3E, (byte) 0x8E, (byte) 0x1C, (byte) 0x91, (byte) 0x2C, (byte) 0xC6, (byte) 0x18, (byte) 0xCB, (byte) 0x22,
                (byte) 0xDD, (byte) 0xE7, (byte) 0xC3, (byte) 0x56, (byte) 0x8E, (byte) 0x90, (byte) 0x02, (byte) 0x2E, (byte) 0x6B, (byte) 0xBA, (byte) 0x77, (byte) 0x02, (byte) 0x02, (byte) 0xE4, (byte) 0x52, (byte) 0x2A,
                (byte) 0x2D, (byte) 0xD6, (byte) 0x23, (byte) 0xD1, (byte) 0x80, (byte) 0xE2, (byte) 0x15, (byte) 0xBD, (byte) 0x1D, (byte) 0x15, (byte) 0x07, (byte) 0xFE, (byte) 0x3D, (byte) 0xC9, (byte) 0x0C, (byte) 0xA3,
                (byte) 0x10, (byte) 0xD2, (byte) 0x7B, (byte) 0x3E, (byte) 0xFC, (byte) 0xCD, (byte) 0x8F, (byte) 0x83, (byte) 0xDE, (byte) 0x30, (byte) 0x52, (byte) 0xCA, (byte) 0xD1, (byte) 0xE4, (byte) 0x89, (byte) 0x38,
                (byte) 0xC6, (byte) 0x8D, (byte) 0x09, (byte) 0x5A, (byte) 0xAC, (byte) 0x91, (byte) 0xB5, (byte) 0xF3, (byte) 0x7E, (byte) 0x28, (byte) 0xBB, (byte) 0x49, (byte) 0xEC, (byte) 0x7E, (byte) 0xD5, (byte) 0x97
        };
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x21, 0x12, 0x31};
        capk_01.CheckSum = new byte[]{(byte) 0xEB, (byte) 0xFA, (byte) 0x0D, (byte) 0x5D, (byte) 0x06, (byte) 0xD8, (byte) 0xCE, (byte) 0x70, (byte) 0x2D, (byte) 0xA3, (byte) 0xEA, (byte) 0xE8, (byte) 0x90, (byte) 0x70, (byte) 0x1D, (byte) 0x45,
                (byte) 0xE2, (byte) 0x74, (byte) 0xC8, (byte) 0x45};
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xEF;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("21766EBB0EE122AFB65D7845B73DB46BAB65427A");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF1;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF3;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("A69AC7603DAF566E972DEDC2CB433E07E8B01A9A");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF5;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A6E6FB72179506F860CCCA8C27F99CECD94C7D4F3191D303BBEE37481C7AA15F233BA755E9E4376345A9A67E7994BDC1C680BB3522D8C93EB0CCC91AD31AD450DA30D337662D19AC03E2B4EF5F6EC18282D491E19767D7B24542DFDEFF6F62185503532069BBB369E3BB9FB19AC6F1C30B97D249EEE764E0BAC97F25C873D973953E5153A42064BBFABFD06A4BB486860BF6637406C9FC36813A4A75F75C31CCA9F69F8DE59ADECEF6BDE7E07800FCBE035D3176AF8473E23E9AA3DFEE221196D1148302677C720CFE2544A03DB553E7F1B8427BA1CC72B0F29B12DFEF4C081D076D353E71880AADFF386352AF0AB7B28ED49E1E672D11F9");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("C2239804C8098170BE52D6D5D4159E81CE8466BF");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF6;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A25A6BD783A5EF6B8FB6F83055C260F5F99EA16678F3B9053E0F6498E82C3F5D1E8C38F13588017E2B12B3D8FF6F50167F46442910729E9E4D1B3739E5067C0AC7A1F4487E35F675BC16E233315165CB142BFDB25E301A632A54A3371EBAB6572DEEBAF370F337F057EE73B4AE46D1A8BC4DA853EC3CC12C8CBC2DA18322D68530C70B22BDAC351DD36068AE321E11ABF264F4D3569BB71214545005558DE26083C735DB776368172FE8C2F5C85E8B5B890CC682911D2DE71FA626B8817FCCC08922B703869F3BAEAC1459D77CD85376BC36182F4238314D6C4212FBDD7F23D3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("502909ED545E3C8DBD00EA582D0617FEE9F6F684");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF7;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("94EA62F6D58320E354C022ADDCF0559D8CF206CD92E869564905CE21D720F971B7AEA374830EBE1757115A85E088D41C6B77CF5EC821F30B1D890417BF2FA31E5908DED5FA677F8C7B184AD09028FDDE96B6A6109850AA800175EABCDBBB684A96C2EB6379DFEA08D32FE2331FE103233AD58DCDB1E6E077CB9F24EAEC5C25AF");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("EEB0DD9B2477BEE3209A914CDBA94C1C4A9BDED9");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF8;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("F06ECC6D2AAEBF259B7E755A38D9A9B24E2FF3DD");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF9;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A99A6D3E071889ED9E3A0C391C69B0B804FC160B2B4BDD570C92DD5A0F45F53E8621F7C96C40224266735E1EE1B3C06238AE35046320FD8E81F8CEB3F8B4C97B940930A3AC5E790086DAD41A6A4F5117BA1CE2438A51AC053EB002AED866D2C458FD73359021A12029A0C043045C11664FE0219EC63C10BF2155BB2784609A106421D45163799738C1C30909BB6C6FE52BBB76397B9740CE064A613FF8411185F08842A423EAD20EDFFBFF1CD6C3FE0C9821479199C26D8572CC8AFFF087A9C3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("336712DCC28554809C6AA9B02358DE6F755164DB");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFA;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("5BED4068D96EA16D2D77E03D6036FC7A160EA99C");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0x00;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("9C6BE5ADB10B4BE3DCE2099B4B210672B89656EBA091204F613ECC623BEDC9C6D77B660E8BAEEA7F7CE30F1B153879A4E36459343D1FE47ACDBD41FCD710030C2BA1D9461597982C6E1BDD08554B726F5EFF7913CE59E79E357295C321E26D0B8BE270A9442345C753E2AA2ACFC9D30850602FE6CAC00C6DDF6B8D9D9B4879B2826B042A07F0E5AE526A3D3C4D22C72B9EAA52EED8893866F866387AC05A1399");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("5D2970E64675727E60460765A8DB75342AE14783");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0x02;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A99A6D3E071889ED9E3A0C391C69B0B804FC160B2B4BDD570C92DD5A0F45F53E8621F7C96C40224266735E1EE1B3C06238AE35046320FD8E81F8CEB3F8B4C97B940930A3AC5E790086DAD41A6A4F5117BA1CE2438A51AC053EB002AED866D2C458FD73359021A12029A0C043045C11664FE0219EC63C10BF2155BB2784609A106421D45163799738C1C30909BB6C6FE52BBB76397B9740CE064A613FF8411185F08842A423EAD20EDFFBFF1CD6C3FE0C9821479199C26D8572CC8AFFF087A9C3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("294BE20239AB15245A63BEA46CC6C175A25562D1");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0x05;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("B9A1D65CAFE06B054EDD7EA82597AB85F130E663");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0xF3;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("94EA62F6D58320E354C022ADDCF0559D8CF206CD92E869564905CE21D720F971B7AEA374830EBE1757115A85E088D41C6B77CF5EC821F30B1D890417BF2FA31E5908DED5FA677F8C7B184AD09028FDDE96B6A6109850AA800175EABCDBBB684A96C2EB6379DFEA08D32FE2331FE103233AD58DCDB1E6E077CB9F24EAEC5C25AF");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("5694B0D278481814A05E12B558CEC1234865AA5D");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0xF7;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("F78113E860F030A872923FCE93E3381C77A42A30");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xB0, 0x12, 0x34, 0x56, 0x78};
        capk_01.KeyID = (byte) 0xF8;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A99A6D3E071889ED9E3A0C391C69B0B804FC160B2B4BDD570C92DD5A0F45F53E8621F7C96C40224266735E1EE1B3C06238AE35046320FD8E81F8CEB3F8B4C97B940930A3AC5E790086DAD41A6A4F5117BA1CE2438A51AC053EB002AED866D2C458FD73359021A12029A0C043045C11664FE0219EC63C10BF2155BB2784609A106421D45163799738C1C30909BB6C6FE52BBB76397B9740CE064A613FF8411185F08842A423EAD20EDFFBFF1CD6C3FE0C9821479199C26D8572CC8AFFF087A9C3");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("66469C88E7DC111529C7D379D7938C8DF3E4C25E");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

    }


    void addPaypassCAPK() {
        int result;
        EmvCAPK capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x00;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = new byte[]{
                (byte) 0xB8, (byte) 0x04, (byte) 0x8A, (byte) 0xBC, (byte) 0x30, (byte) 0xC9, (byte) 0x0D, (byte) 0x97,
                (byte) 0x63, (byte) 0x36, (byte) 0x54, (byte) 0x3E, (byte) 0x3F, (byte) 0xD7, (byte) 0x09, (byte) 0x1C,
                (byte) 0x8F, (byte) 0xE4, (byte) 0x80, (byte) 0x0D, (byte) 0xF8, (byte) 0x20, (byte) 0xED, (byte) 0x55,
                (byte) 0xE7, (byte) 0xE9, (byte) 0x48, (byte) 0x13, (byte) 0xED, (byte) 0x00, (byte) 0x55, (byte) 0x5B,
                (byte) 0x57, (byte) 0x3F, (byte) 0xEC, (byte) 0xA3, (byte) 0xD8, (byte) 0x4A, (byte) 0xF6, (byte) 0x13,
                (byte) 0x1A, (byte) 0x65, (byte) 0x1D, (byte) 0x66, (byte) 0xCF, (byte) 0xF4, (byte) 0x28, (byte) 0x4F,
                (byte) 0xB1, (byte) 0x3B, (byte) 0x63, (byte) 0x5E, (byte) 0xDD, (byte) 0x0E, (byte) 0xE4, (byte) 0x01,
                (byte) 0x76, (byte) 0xD8, (byte) 0xBF, (byte) 0x04, (byte) 0xB7, (byte) 0xFD, (byte) 0x1C, (byte) 0x7B,
                (byte) 0xAC, (byte) 0xF9, (byte) 0xAC, (byte) 0x73, (byte) 0x27, (byte) 0xDF, (byte) 0xAA, (byte) 0x8A,
                (byte) 0xA7, (byte) 0x2D, (byte) 0x10, (byte) 0xDB, (byte) 0x3B, (byte) 0x8E, (byte) 0x70, (byte) 0xB2,
                (byte) 0xDD, (byte) 0xD8, (byte) 0x11, (byte) 0xCB, (byte) 0x41, (byte) 0x96, (byte) 0x52, (byte) 0x5E,
                (byte) 0xA3, (byte) 0x86, (byte) 0xAC, (byte) 0xC3, (byte) 0x3C, (byte) 0x0D, (byte) 0x9D, (byte) 0x45,
                (byte) 0x75, (byte) 0x91, (byte) 0x64, (byte) 0x69, (byte) 0xC4, (byte) 0xE4, (byte) 0xF5, (byte) 0x3E,
                (byte) 0x8E, (byte) 0x1C, (byte) 0x91, (byte) 0x2C, (byte) 0xC6, (byte) 0x18, (byte) 0xCB, (byte) 0x22,
                (byte) 0xDD, (byte) 0xE7, (byte) 0xC3, (byte) 0x56, (byte) 0x8E, (byte) 0x90, (byte) 0x02, (byte) 0x2E,
                (byte) 0x6B, (byte) 0xBA, (byte) 0x77, (byte) 0x02, (byte) 0x02, (byte) 0xE4, (byte) 0x52, (byte) 0x2A,
                (byte) 0x2D, (byte) 0xD6, (byte) 0x23, (byte) 0xD1, (byte) 0x80, (byte) 0xE2, (byte) 0x15, (byte) 0xBD,
                (byte) 0x1D, (byte) 0x15, (byte) 0x07, (byte) 0xFE, (byte) 0x3D, (byte) 0xC9, (byte) 0x0C, (byte) 0xA3,
                (byte) 0x10, (byte) 0xD2, (byte) 0x7B, (byte) 0x3E, (byte) 0xFC, (byte) 0xCD, (byte) 0x8F, (byte) 0x83,
                (byte) 0xDE, (byte) 0x30, (byte) 0x52, (byte) 0xCA, (byte) 0xD1, (byte) 0xE4, (byte) 0x89, (byte) 0x38,
                (byte) 0xC6, (byte) 0x8D, (byte) 0x09, (byte) 0x5A, (byte) 0xAC, (byte) 0x91, (byte) 0xB5, (byte) 0xF3,
                (byte) 0x7E, (byte) 0x28, (byte) 0xBB, (byte) 0x49, (byte) 0xEC, (byte) 0x7E, (byte) 0xD5, (byte) 0x97
        };
        capk_01.Exponent = new byte[]{0x05};
        capk_01.ExpDate = new byte[]{0x21, 0x12, 0x31};
        capk_01.CheckSum = new byte[]{(byte) 0xEB, (byte) 0xFA, (byte) 0x0D, (byte) 0x5D, (byte) 0x06, (byte) 0xD8, (byte) 0xCE, (byte) 0x70,
                (byte) 0x2D, (byte) 0xA3, (byte) 0xEA, (byte) 0xE8, (byte) 0x90, (byte) 0x70, (byte) 0x1D, (byte) 0x45,
                (byte) 0xE2, (byte) 0x74, (byte) 0xC8, (byte) 0x45
        };
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x06;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = new byte[]{
                (byte) 0xCB, (byte) 0x26, (byte) 0xFC, (byte) 0x83, (byte) 0x0B, (byte) 0x43, (byte) 0x78, (byte) 0x5B,
                (byte) 0x2B, (byte) 0xCE, (byte) 0x37, (byte) 0xC8, (byte) 0x1E, (byte) 0xD3, (byte) 0x34, (byte) 0x62,
                (byte) 0x2F, (byte) 0x96, (byte) 0x22, (byte) 0xF4, (byte) 0xC8, (byte) 0x9A, (byte) 0xAE, (byte) 0x64,
                (byte) 0x10, (byte) 0x46, (byte) 0xB2, (byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x88, (byte) 0x3F,
                (byte) 0x30, (byte) 0x7F, (byte) 0xB7, (byte) 0xC9, (byte) 0x74, (byte) 0x16, (byte) 0x2D, (byte) 0xA7,
                (byte) 0x2F, (byte) 0x7A, (byte) 0x4E, (byte) 0xC7, (byte) 0x5D, (byte) 0x9D, (byte) 0x65, (byte) 0x73,
                (byte) 0x36, (byte) 0x86, (byte) 0x5B, (byte) 0x8D, (byte) 0x30, (byte) 0x23, (byte) 0xD3, (byte) 0xD6,
                (byte) 0x45, (byte) 0x66, (byte) 0x76, (byte) 0x25, (byte) 0xC9, (byte) 0xA0, (byte) 0x7A, (byte) 0x6B,
                (byte) 0x7A, (byte) 0x13, (byte) 0x7C, (byte) 0xF0, (byte) 0xC6, (byte) 0x41, (byte) 0x98, (byte) 0xAE,
                (byte) 0x38, (byte) 0xFC, (byte) 0x23, (byte) 0x80, (byte) 0x06, (byte) 0xFB, (byte) 0x26, (byte) 0x03,
                (byte) 0xF4, (byte) 0x1F, (byte) 0x4F, (byte) 0x3B, (byte) 0xB9, (byte) 0xDA, (byte) 0x13, (byte) 0x47,
                (byte) 0x27, (byte) 0x0F, (byte) 0x2F, (byte) 0x5D, (byte) 0x8C, (byte) 0x60, (byte) 0x6E, (byte) 0x42,
                (byte) 0x09, (byte) 0x58, (byte) 0xC5, (byte) 0xF7, (byte) 0xD5, (byte) 0x0A, (byte) 0x71, (byte) 0xDE,
                (byte) 0x30, (byte) 0x14, (byte) 0x2F, (byte) 0x70, (byte) 0xDE, (byte) 0x46, (byte) 0x88, (byte) 0x89,
                (byte) 0xB5, (byte) 0xE3, (byte) 0xA0, (byte) 0x86, (byte) 0x95, (byte) 0xB9, (byte) 0x38, (byte) 0xA5,
                (byte) 0x0F, (byte) 0xC9, (byte) 0x80, (byte) 0x39, (byte) 0x3A, (byte) 0x9C, (byte) 0xBC, (byte) 0xE4,
                (byte) 0x4A, (byte) 0xD2, (byte) 0xD6, (byte) 0x4F, (byte) 0x63, (byte) 0x0B, (byte) 0xB3, (byte) 0x3A,
                (byte) 0xD3, (byte) 0xF5, (byte) 0xF5, (byte) 0xFD, (byte) 0x49, (byte) 0x5D, (byte) 0x31, (byte) 0xF3,
                (byte) 0x78, (byte) 0x18, (byte) 0xC1, (byte) 0xD9, (byte) 0x40, (byte) 0x71, (byte) 0x34, (byte) 0x2E,
                (byte) 0x07, (byte) 0xF1, (byte) 0xBE, (byte) 0xC2, (byte) 0x19, (byte) 0x4F, (byte) 0x60, (byte) 0x35,
                (byte) 0xBA, (byte) 0x5D, (byte) 0xED, (byte) 0x39, (byte) 0x36, (byte) 0x50, (byte) 0x0E, (byte) 0xB8,
                (byte) 0x2D, (byte) 0xFD, (byte) 0xA6, (byte) 0xE8, (byte) 0xAF, (byte) 0xB6, (byte) 0x55, (byte) 0xB1,
                (byte) 0xEF, (byte) 0x3D, (byte) 0x0D, (byte) 0x7E, (byte) 0xBF, (byte) 0x86, (byte) 0xB6, (byte) 0x6D,
                (byte) 0xD9, (byte) 0xF2, (byte) 0x9F, (byte) 0x6B, (byte) 0x1D, (byte) 0x32, (byte) 0x4F, (byte) 0xE8,
                (byte) 0xB2, (byte) 0x6C, (byte) 0xE3, (byte) 0x8A, (byte) 0xB2, (byte) 0x01, (byte) 0x3D, (byte) 0xD1,
                (byte) 0x3F, (byte) 0x61, (byte) 0x1E, (byte) 0x7A, (byte) 0x59, (byte) 0x4D, (byte) 0x67, (byte) 0x5C,
                (byte) 0x44, (byte) 0x32, (byte) 0x35, (byte) 0x0E, (byte) 0xA2, (byte) 0x44, (byte) 0xCC, (byte) 0x34,
                (byte) 0xF3, (byte) 0x87, (byte) 0x3C, (byte) 0xBA, (byte) 0x06, (byte) 0x59, (byte) 0x29, (byte) 0x87,
                (byte) 0xA1, (byte) 0xD7, (byte) 0xE8, (byte) 0x52, (byte) 0xAD, (byte) 0xC2, (byte) 0x2E, (byte) 0xF5,
                (byte) 0xA2, (byte) 0xEE, (byte) 0x28, (byte) 0x13, (byte) 0x20, (byte) 0x31, (byte) 0xE4, (byte) 0x8F,
                (byte) 0x74, (byte) 0x03, (byte) 0x7E, (byte) 0x3B, (byte) 0x34, (byte) 0xAB, (byte) 0x74, (byte) 0x7F

        };
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x21, 0x12, 0x31};
        capk_01.CheckSum = new byte[]{(byte) 0xF9, (byte) 0x10, (byte) 0xA1, (byte) 0x50, (byte) 0x4D, (byte) 0x5F, (byte) 0xFB, (byte) 0x79,
                (byte) 0x3D, (byte) 0x94, (byte) 0xF3, (byte) 0xB5, (byte) 0x00, (byte) 0x76, (byte) 0x5E, (byte) 0x1A,
                (byte) 0xBC, (byte) 0xAD, (byte) 0x72, (byte) 0xD9
        };
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x09;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("967B6264436C96AA9305776A5919C70DA796340F9997A6C6EF7BEF1D4DBF9CB4289FB7990ABFF1F3AE692F12844B2452A50AE075FB327976A40E8028F279B1E3CCB623957D696FC1225CA2EC950E2D415E9AA931FF18B13168D661FBD06F0ABB");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("1D90595C2EF9FC6E71B0C721118333DF8A71FE21");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x22;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("BBE43877CC28C0CE1E14BC14E8477317E218364531D155BB8AC5B63C0D6E284DD24259193899F9C04C30BAF167D57929451F67AEBD3BBD0D41444501847D8F02F2C2A2D14817D97AE2625DC163BF8B484C40FFB51749CEDDE9434FB2A0A41099");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("008C39B1D119498268B07843349427AC6E98F807");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0x52;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("B831414E0B4613922BD35B4B36802BC1E1E81C95A27C958F5382003DF646154CA92FC1CE02C3BE047A45E9B02A9089B4B90278237C965192A0FCC86BB49BC82AE6FDC2DE709006B86C7676EFDF597626FAD633A4F7DC48C445D37EB55FCB3B1ABB95BAAA826D5390E15FD14ED403FA2D0CB841C650609524EC555E3BC56CA957");
        capk_01.Exponent = new byte[]{0x11};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("DEB81EDB2626A4BB6AE23B77D19A77539D0E6716");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xF0;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("7563C51B5276AA6370AB8405522414645832B6BEF2A989C771475B2E8DC654DC8A5BFF9E28E31FF1A370A40DC3FFEB06BC85487D5F1CB61C2441FD71CBCD05D883F8DE413B243AFC9DCA768B061E35B884B5D21B6B016AA36BA12DABCFE49F8E528C893C34C7D4793977E4CC99AB09640D9C7AAB7EC5FF3F40E3D4D18DF7E3A7");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("AE667445F8DE6F82C38800E5EBABA322F03F58F2");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFA;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("9C6BE5ADB10B4BE3DCE2099B4B210672B89656EBA091204F613ECC623BEDC9C6D77B660E8BAEEA7F7CE30F1B153879A4E36459343D1FE47ACDBD41FCD710030C2BA1D9461597982C6E1BDD08554B726F5EFF7913CE59E79E357295C321E26D0B8BE270A9442345C753E2AA2ACFC9D30850602FE6CAC00C6DDF6B8D9D9B4879B2826B042A07F0E5AE526A3D3C4D22C72B9EAA52EED8893866F866387AC05A1399");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("0ABCADAD2C7558CA9C7081AE55DDDC714F8D45F8");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFB;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("A9548DFB398B48123FAF41E6CFA4AE1E2352B518AB4BCEFECDB0B3EDEC090287D88B12259F361C1CC088E5F066494417E8EE8BBF8991E2B32FF16F994697842B3D6CB37A2BB5742A440B6356C62AA33DB3C455E59EDDF7864701D03A5B83EE9E9BD83AB93302AC2DFE63E66120B051CF081F56326A71303D952BB336FF12610D");
        capk_01.Exponent = new byte[]{0x02};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("6C7289632919ABEE6E1163D7E6BF693FD88EBD35");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFC;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("B37BFD2A9674AD6221C1A001081C62653DC280B0A9BD052C677C913CE7A0D902E77B12F4D4D79037B1E9B923A8BB3FAC3C612045BB3914F8DF41E9A1B61BFA5B41705A691D09CE6F530FE48B30240D98F4E692FFD6AADB87243BA8597AB237586ECF258F4148751BE5DA5A3BE6CC34BD");
        capk_01.Exponent = new byte[]{0x02};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("7FB377EEBBCF7E3A6D04015D10E1BDCB15E21B80");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFD;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("B3572BA49AE4C7B7A0019E5189E142CFCDED9498DDB5F0470567AB0BA713B8DA226424622955B54B937ABFEFAAD97919E377621E22196ABC1419D5ADC123484209EA7CB7029E66A0D54C5B45C8AD615AEDB6AE9E0A2F75310EA8961287241245");
        capk_01.Exponent = new byte[]{0x02};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("23CF0D702E0AEFE518E4FA6B836D3CD45B8AAA71");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

        //==================================================
        capk_01 = new EmvCAPK();
        capk_01.RID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
        capk_01.KeyID = (byte) 0xFF;
        capk_01.HashInd = (byte) 0x01;
        capk_01.ArithInd = (byte) 0x01;
        capk_01.Modul = StringUtil.hexStringToByte("B855CC64313AF99C453D181642EE7DD21A67D0FF50C61FE213BCDC18AFBCD07722EFDD2594EFDC227DA3DA23ADCC90E3FA907453ACC954C47323BEDCF8D4862C457D25F47B16D7C3502BE081913E5B0482D838484065DA5F6659E00A9E5D570ADA1EC6AF8C57960075119581FC81468D");
        capk_01.Exponent = new byte[]{0x03};
        capk_01.ExpDate = new byte[]{0x25, 0x12, 0x31};
        capk_01.CheckSum = StringUtil.hexStringToByte("B4E769CECF7AAC4783F305E0B110602A07A6355B");
        result = EmvService.Emv_AddCapk(capk_01);
        AppendDis("add Capk " + StringUtil.bytesToHexString_upcase(capk_01.RID) + "(" + Integer.toHexString(capk_01.KeyID) + ")" + " : " + result);

    }
}

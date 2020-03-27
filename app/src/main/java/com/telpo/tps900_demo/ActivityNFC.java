package com.telpo.tps900_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.telpo.emv.EmvCandidateApp;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvPinData;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvServiceListener;
import com.telpo.emv.PaypassErrorData;
import com.telpo.emv.PaypassOutCome;
import com.telpo.emv.PaypassParam;
import com.telpo.emv.PaypassResult;
import com.telpo.emv.PaypassUserData;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityNFC extends Activity {
    TextView title_tv;
    Context context;
    EmvService emvService;

    Button bn_emvDeviceOpen, bn_emvDeviceClose ,bn_CheckKernelID,bn_CheckKernelIDEx;

    Button[] buttons;

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
            AmountData.Amount = 12346;
            AmountData.TransCurrCode = (short) 840;
            AmountData.ReferCurrCode = (short) 840;
            AmountData.TransCurrExp = (byte) 2;
            AmountData.ReferCurrExp = (byte) 2;
            AmountData.ReferCurrCon = 0;
            AmountData.CashbackAmount = 11111;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_detect);
        context = ActivityNFC.this;
        viewInit();
        ActivityInit();
    }

    private void ActivityInit() {
        EmvService.Emv_SetDebugOn(1);
        emvService = EmvService.getInstance();
        emvService.setListener(listener);
    }

    void viewInit() {

        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("NFC DETECT");
        bn_emvDeviceOpen = (Button) findViewById(R.id.bn_emvDeviceOpen);
        bn_emvDeviceClose = (Button) findViewById(R.id.bn_emvDeviceClose);
        bn_CheckKernelID = (Button)findViewById(R.id.bn_CheckKernelID);
        bn_CheckKernelIDEx = (Button)findViewById(R.id.bn_CheckKernelIDEx);
        buttons = new Button[]{bn_CheckKernelID, bn_CheckKernelIDEx};

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



        bn_CheckKernelID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int ret;
                        AppendDis("========================");
                        AppendDis("try to detect card kernel");
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
                            ret = detectCardKernel();
                            switch(ret){
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_VISA:
                                    AppendDis("This is a Visa card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_MASTER:
                                    AppendDis("This is a Master card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_UNIONPAY:
                                    AppendDis("This is a Unionpay card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_RUPAY:
                                    AppendDis("This is a Rupay card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_AMEX:
                                    AppendDis("This is a Amex card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_DISCOVER:
                                    AppendDis("This is a Discovery card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_JCB:
                                    AppendDis("This is a JCB card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_UNKNOWN:
                                    AppendDis("This is an unknown card" );
                                    break;
                                default:
                                    AppendDis("card kernel error:" + ret);
                                    break;
                            }
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

        bn_CheckKernelIDEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int ret;
                        AppendDis("========================");
                        AppendDis("try to detect card kernel");
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
                            ret = detectCardKernelEX();
                            switch(ret){
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_VISA:
                                    AppendDis("This is a Visa card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_MASTER:
                                    AppendDis("This is a Master card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_UNIONPAY:
                                    AppendDis("This is a Unionpay card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_RUPAY:
                                    AppendDis("This is a Rupay card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_AMEX:
                                    AppendDis("This is a Amex card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_DISCOVER:
                                    AppendDis("This is a Discovery card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_JCB:
                                    AppendDis("This is a JCB card" );
                                    break;
                                case EmvService.NFC_KERNEL_DEFAUT_CARD_UNKNOWN:
                                    AppendDis("This is an unknown card" );
                                    break;
                                default:
                                    AppendDis("card kernel error:" + ret);
                                    break;
                            }
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
        PaypassParam paypassParam = new PaypassParam();
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

    int detectCardKernel(){
        int ret = -1;
        ret = emvService.NFC_CheckKernelID();
        return ret;
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

    int detectCardKernelEX(){
        int ret = -1;
        EmvApp app_visa = new EmvApp();
        app_visa.AID = new byte[]{(byte) 0xA0,0x00,0x00,0x00,0x03};

        EmvApp app_master1 = new EmvApp();
        app_master1.AID = new byte[]{(byte)0xA0,0x00,0x00,0x00,0x04};

        EmvApp app_master2 = new EmvApp();
        app_master2.AID = new byte[]{(byte)0xB0,0x12,0x34,0x56,0x78};

        EmvApp app_unionpay = new EmvApp();
        app_unionpay.AID = new byte[]{(byte)0xA0,0x00,0x00,0x03,0x33};

        EmvApp app_rupay = new EmvApp();
        app_rupay.AID = new byte[]{(byte)0xA0,0x00,0x00,0x05,0x24};

        EmvApp app_jcb = new EmvApp();
        app_jcb.AID = new byte[]{(byte)0xA0,0x00,0x00,0x00,0x65};

        EmvApp app_amex = new EmvApp();
        app_amex.AID = new byte[]{(byte)0xA0,0x00,0x00,0x00,0x25};

        EmvApp app_discaver = new EmvApp();
        app_discaver.AID = new byte[]{(byte)0xA0,0x00,0x00,0x03,0x24};
        EmvApp[] list = new EmvApp[]{app_visa,app_master1,app_unionpay,app_rupay,app_amex,app_discaver,app_jcb,app_master2};
        ret = emvService.NFC_CheckKernelIDEx(list);
        if (ret == 7){
            ret = 1;
        }
        Log.w("card kernel", "detectCardKernelEX: " + ret);
        return ret;
    }



}

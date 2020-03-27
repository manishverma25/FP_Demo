package com.telpo.tps900_demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.QvsdcParam;
import com.telpo.emv.util.StringUtil;
import com.telpo.pinpad.PinParam;
import com.telpo.pinpad.PinpadService;

/**
 * Created by Administrator on 2017/4/5 0005.
 */
public class TaskReadVisaPayvaveCard extends AsyncTask<Integer, String, Integer> {

    public static int Mag = 0;
    public static int IC = 1;
    public static int Nfc = 2;

    EmvService emvService;
    Context context;
    ProgressDialog dialog = null;
    String[] data = new String[3];

    int event;

    boolean userCancel = false;

    long startMs;
    int ret;

    public TaskReadVisaPayvaveCard(Context context, EmvService emvService) {
        this.emvService = emvService;
        this.context = context;
        dialog = new ProgressDialog(context);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                deviceClose();
                userCancel = true;
                cancel(true);
            }
        });
        //dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(false);
        dialog.setTitle("Read card");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("abc");

    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        dialog.show();

    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        deviceClose();
        dialog.dismiss();
        if (event == Nfc) {
            new AlertDialog.Builder(context)
                    .setMessage("NFC process finish:" + ret +"(0x"+ Integer.toHexString(ret)+ ")\n NeedPin? "+emvService.qVsdc_IsNeedPin() + "\n Need Signature? " +emvService.qVsdc_IsNeedSignture())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        dialog.setMessage(values[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Integer doInBackground(Integer... params) {

        try {
            publishProgress("open device..");
            openDevice();
            Thread.sleep(500);

            publishProgress("detect card ..");
            event = detectCard();

            if(event == Nfc){
                publishProgress("Read NFC card ..");
                QvsdcParam qvsdcParam = new QvsdcParam();
                qvsdcParam.AMOUNT_Amount = 1000;
                qvsdcParam.AMOUNT_CashbackAmount = 0;
                qvsdcParam.AMOUNT_CurrCode = 840;
                qvsdcParam.AMOUNT_CurrExp = 2;

                qvsdcParam.SUPPORT_MSD = 0;
                qvsdcParam.SUPPORT_EMV = 0;
                qvsdcParam.SUPPORT_Signature = 1;
                qvsdcParam.SUPPORT_OnlinePIN = 1;
                qvsdcParam.SUPPORT_CashControl = 0;
                qvsdcParam.SUPPORT_CashbackControl = 0;
                qvsdcParam.SUPPORT_ZeroAmtCheck = 1;
                qvsdcParam.SUPPORT_ZeroCheckType = 0;

                ret = emvService.qVsdc_TransInit(qvsdcParam);
                Log.w("readcard", "qVsdc_TransInit: " + ret);

                {
                    EmvParam mEMVParam;
                    mEMVParam = new EmvParam();
                    mEMVParam.MerchName = "Telpo".getBytes();
                    mEMVParam.MerchId = "123456789012345".getBytes();
                    mEMVParam.TermId = "12345678".getBytes();
                    mEMVParam.TerminalType = 0x22;
                    mEMVParam.Capability = new byte[]{(byte) 0xE0, (byte) 0xE9, (byte) 0xC8};
                    mEMVParam.ExCapability = new byte[]{(byte) 0xE0, 0x00, (byte) 0xF0, (byte) 0xA0, 0x01};
                    mEMVParam.CountryCode = new byte[]{(byte) 0x08, (byte) 0x40};

                    mEMVParam.TransType = 0x00; //0x31
                    emvService.Emv_SetParam(mEMVParam);
                }
                startMs = System.currentTimeMillis();

                ret = emvService.qVsdc_Preprocess();
                Log.w("readcard", "qVsdc_Preprocess: " + ret);
                ret  = emvService.qVsdc_StartApp();
                Log.w("readcard", "qVsdc_StartApp: " + ret);
                {
                    EmvTLV tag9F5D = new EmvTLV(0x9F5D);
                    int ret = emvService.Emv_GetTLV(tag9F5D);
                    if(ret == EmvService.EMV_TRUE){
                        Log.w("9F5D","9F5D:"+ StringUtil.bytesToHexString_upcase(tag9F5D.Value));

                    }else {
                        Log.w("9F5D","no 9F5D");
                    }
                }
                if(ret == EmvService.QVSDC_ONLINE_APPROVE){
                    //if (emvService.qPboc_IsNeedPin() == EmvService.EMV_TRUE){
                    if (false){
                        final boolean[] isrunning = {true};
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int ret;
                                PinParam param = new PinParam(context);

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
                                param.KeyIndex = 3;
                                param.WaitSec = 60;
                                param.MaxPinLen = 6;
                                param.MinPinLen= 4;
                                param.IsShowCardNo = 1;
                                param.Amount = "1.00";
                                ret = PinpadService.Open(context);
                                Log.w("pin", "open: " + ret);
                                ret = PinpadService.TP_PinpadGetPin(param);
                                Log.w("pin", "getpin: " + ret);
                                isrunning[0] = false;

                            }
                        }).start();


                        while (isrunning[0]){
                            Thread.sleep(1000);
                        }
                    }
                }




            }
            //Thread.sleep(500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    void openDevice() {
        int ret;
        ret = EmvService.NfcOpenReader(1000);
        DefaultAPPCAPK.Log("NfcOpenReader:" + ret);

    }

    void deviceClose() {
        int ret;
        ret = EmvService.NfcCloseReader();
        DefaultAPPCAPK.Log("NfcCloseReader:" + ret);

    }

    int detectCard() {
        int ret;
        while (true) {
            if (userCancel) {
                return IC;
            }

            ret = EmvService.NfcCheckCard(1000);
            DefaultAPPCAPK.Log("NfcCheckCard:" + ret);
            if (ret == 0) {
                return Nfc;
            }
        }


    }
}

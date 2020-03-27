package com.telpo.tps900_demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.telpo.emv.PaypassParam;
import com.telpo.emv.util.StringUtil;

public class ActivityPaypassParam extends Activity {
    EditText TermAddCaps,AppVersion,MagAppVersion,UDOL,KernelID,CardInputCap,SecurityCap,MobileSupport,TerminalType,
            TermCountryCode, HoldTimeMs, MessHoldTimeMs,TimeOutSec, FailedMsCount, TornLifetimeSec,TornLogCount,
            RRPrExpectCAPDU, RRPrExpectRAPDU, RRPrMaxGrace ,RRPrMinGrace, RRPrAccuracyThreshold, RRPrMismatchThreshold,
            Pre_Balance, Post_Balance, KernelConfig, MagCVM_Cap, MagNoCVM_Cap, CVM_Cap, NoCVM_Cap, FloorLimit,
            NoOnDevCvmTransLimit, OnDevCvmTransLimit, CvmRequiedLimit;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypassparam);
        TermAddCaps = (EditText)findViewById(R.id.et_TermAddCaps);      //Additional Terminal Capabilities 	9F40	[5]           '0000000000'
        AppVersion = (EditText)findViewById(R.id.et_AppVersion) ;          //Application Version Number (Reader) 	9F09[2]    '0002'
        MagAppVersion = (EditText)findViewById(R.id.et_MagAppVersion);    //Mag-stripe Application Version Number (Reader) 	9F6D[2]	'0001'

        UDOL = (EditText)findViewById(R.id.et_UDOL);                  //Default UDOL 	DF811A	'9F6A04' [3]
        KernelID = (EditText)findViewById(R.id.et_KernelID);                   //Kernel ID 	DF810C	'02'
        CardInputCap = (EditText)findViewById(R.id.et_CardInputCap);             //Card Data Input Capability 	DF8117	 '00'
        SecurityCap = (EditText)findViewById(R.id.et_SecurityCap);                //Security Capability 	DF811F	    '00'
        MobileSupport = (EditText)findViewById(R.id.et_MobileSupport);          //Mobile Support Indicator	9F7E     	'01'
        TerminalType = (EditText)findViewById(R.id.et_TerminalType);                //Terminal Type 	9F35  	'00'
        TermCountryCode = (EditText)findViewById(R.id.et_TermCountryCode);                    //Terminal Country Code 	9F1A	     '0000'

        HoldTimeMs = (EditText)findViewById(R.id.et_HoldTimeMs); ;           //Hold Time Value 	DF8130  	'0D'
        MessHoldTimeMs = (EditText)findViewById(R.id.et_MessHoldTimeMs);                      //Message Hold Time 	DF812D	'000013'
        TimeOutSec = (EditText)findViewById(R.id.et_TimeOutSec); ;                      //Time Out Value 	DF8127	    '01F4'
        FailedMsCount = (EditText)findViewById(R.id.et_FailedMsCount);
//
        TornLifetimeSec = (EditText)findViewById(R.id.et_TornLifetimeSec);                           //Max Lifetime of Torn Transaction Log Record 	DF811C	'012C'
        TornLogCount = (EditText)findViewById(R.id.et_TornLogCount); //Max Number of Torn Transaction Log Records 	DF811D	'00'

        RRPrExpectCAPDU = (EditText)findViewById(R.id.et_RRPrExpectCAPDU);                    //Terminal Expected Transmission Time For Relay Resistance C-APDU 	DF8134	   '0012'
        RRPrExpectRAPDU = (EditText)findViewById(R.id.et_RRPrExpectRAPDU);               //Terminal Expected Transmission Time For Relay Resistance R-APDU 	DF8135	   '0018'
        RRPrMaxGrace = (EditText)findViewById(R.id.et_RRPrMaxGrace);                    //Maximum Relay Resistance Grace Period 	DF8133   	'0032'
        RRPrMinGrace = (EditText)findViewById(R.id.et_RRPrMinGrace) ;                    //Minimum Relay Resistance Grace Period 	DF8132	  '0014'
        RRPrAccuracyThreshold = (EditText)findViewById(R.id.et_RRPrAccuracyThreshold);                  //Relay Resistance Accuracy Threshold 	DF8136  	'012C'
        RRPrMismatchThreshold = (EditText)findViewById(R.id.et_RRPrMismatchThreshold) ;   //Relay Resistance Transmission Time Mismatch Threshold 	DF8137  '32'

        Pre_Balance = (EditText)findViewById(R.id.et_Pre_Balance) ;                     //在GAC之前读取余额
        Post_Balance = (EditText)findViewById(R.id.et_Post_Balance) ;                   //在GAC之后读取余额
        KernelConfig = (EditText)findViewById(R.id.et_KernelConfig) ;                 //Kernel Configuration 	DF811B	'00'
        MagCVM_Cap = (EditText)findViewById(R.id.et_MagCVM_Cap) ;          //Mag-stripe CVM Capability – CVM Required 	DF811E	'F0'
        MagNoCVM_Cap = (EditText)findViewById(R.id.et_MagNoCVM_Cap);      //Mag-stripe CVM Capability – No CVM Required 	DF812C	'F0'
        CVM_Cap = (EditText)findViewById(R.id.et_CVM_Cap) ;               //CVM Capability – CVM Required 	DF8118	           '00'
        NoCVM_Cap = (EditText)findViewById(R.id.et_NoCVM_Cap) ;           //CVM Capability – No CVM Required 	DF8119	   '00'

        FloorLimit = (EditText)findViewById(R.id.et_FloorLimit) ;               //Reader Contactless Floor Limit 	DF8123	       '000000000000'
        NoOnDevCvmTransLimit = (EditText)findViewById(R.id.et_NoOnDevCvmTransLimit);    //Reader Contactless Transaction Limit (No On-device CVM) 	DF8124  '000000000000'
        OnDevCvmTransLimit = (EditText)findViewById(R.id.et_OnDevCvmTransLimit);        //Reader Contactless Transaction Limit (On-device CVM) 	DF8125          '000000000000'
        CvmRequiedLimit = (EditText)findViewById(R.id.et_CvmRequiedLimit);           //Reader CVM Required Limit 	DF8126	   '000000000000'
        init();
        Button bt_sava = (Button)findViewById(R.id.bt_sava);
        bt_sava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savaParam();
                finish();
            }
        });
    }
    private void init(){

        this.TermAddCaps.setText("0000000000");
        this.AppVersion.setText("0002") ;
        this.MagAppVersion.setText("0001");
        this.UDOL.setText("9F6A04");
        this.KernelID.setText("02");
        this.SecurityCap.setText("08");
        this.MobileSupport.setText("01");
        this.TerminalType.setText("22");
        this.HoldTimeMs.setText("0D");
        this.MessHoldTimeMs.setText("13");
        this.TimeOutSec.setText("01F4");
        this.TornLifetimeSec.setText("012C");
        this.TornLogCount.setText("01");

        this.RRPrExpectCAPDU.setText("12");
        this.RRPrExpectRAPDU.setText("18");               //Terminal Expected Transmission Time For Relay Resistance R-APDU 	DF8135	   '0018'
        this.RRPrMaxGrace.setText("32");                    //Maximum Relay Resistance Grace Period 	DF8133   	'0032'
        this.RRPrMinGrace.setText("14");                   //Minimum Relay Resistance Grace Period 	DF8132	  '0014'
        this.RRPrAccuracyThreshold.setText("012C");                 //Relay Resistance Accuracy Threshold 	DF8136  	'012C'
        this.RRPrMismatchThreshold.setText("32");  //Relay Resistance Transmission Time Mismatch Threshold 	DF8137  '32'

        this.KernelConfig.setText("30");

        this.MagCVM_Cap.setText("F0");
        this.MagNoCVM_Cap.setText("F0");
        this.CVM_Cap.setText("60");
        this.NoCVM_Cap.setText("08");

        this.FloorLimit.setText("2710");
        this.NoOnDevCvmTransLimit.setText("7530");
        this.OnDevCvmTransLimit.setText("C350");
        this.CvmRequiedLimit.setText("03E8");
    }
    private void savaParam(){
        PaypassParam param = new PaypassParam();
        String strTxt;
        strTxt = TermAddCaps.getText().toString();
        param.TermAddCaps = StringUtil.hexStringToByte(strTxt);
        strTxt = AppVersion.getText().toString();
        param.AppVersion = StringUtil.hexStringToByte(strTxt);
        strTxt = MagAppVersion.getText().toString();
        param.MagAppVersion = StringUtil.hexStringToByte(strTxt);

        strTxt = UDOL.getText().toString();
        param.UDOL = StringUtil.hexStringToByte(strTxt);
        strTxt = KernelID.getText().toString();
        param.KernelID = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = CardInputCap.getText().toString();
        param.CardInputCap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = SecurityCap.getText().toString();
        param.SecurityCap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = MobileSupport.getText().toString();
        param.MobileSupport = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = TerminalType.getText().toString();
        param.TerminalType = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = TermCountryCode.getText().toString();
        param.TermCountryCode = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));

        strTxt = HoldTimeMs.getText().toString();
        param.HoldTimeMs = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = MessHoldTimeMs.getText().toString();
        param.MessHoldTimeMs = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = TimeOutSec.getText().toString();
        param.TimeOutSec = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = FailedMsCount.getText().toString();
        param.FailedMsCount = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));

        strTxt = TornLifetimeSec.getText().toString();
        param.TornLifetimeSec = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = TornLogCount.getText().toString();
        param.TornLogCount = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));

        strTxt = RRPrExpectCAPDU.getText().toString();
        param.RRPrExpectCAPDU = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = RRPrExpectRAPDU.getText().toString();
        param.RRPrExpectRAPDU = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = RRPrMaxGrace.getText().toString();
        param.RRPrMaxGrace = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = RRPrMinGrace.getText().toString();
        param.RRPrMinGrace = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = RRPrAccuracyThreshold.getText().toString();
        param.RRPrAccuracyThreshold = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = RRPrMismatchThreshold.getText().toString();
        param.RRPrMismatchThreshold = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));

        strTxt = Pre_Balance.getText().toString();
        param.Pre_Balance = StringUtil.hexStringToByte(strTxt)[0];
        strTxt = Post_Balance.getText().toString();
        param.Post_Balance = StringUtil.hexStringToByte(strTxt)[0];
        strTxt = KernelConfig.getText().toString();
        param.KernelConfig = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = MagCVM_Cap.getText().toString();
        param.MagCVM_Cap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = MagNoCVM_Cap.getText().toString();
        param.MagNoCVM_Cap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = CVM_Cap.getText().toString();
        param.CVM_Cap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = NoCVM_Cap.getText().toString();
        param.NoCVM_Cap = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));

        strTxt = FloorLimit.getText().toString();
        param.FloorLimit = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = NoOnDevCvmTransLimit.getText().toString();
        param.NoOnDevCvmTransLimit = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = OnDevCvmTransLimit.getText().toString();
        param.OnDevCvmTransLimit = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        strTxt = CvmRequiedLimit.getText().toString();
        param.CvmRequiedLimit = StringUtil.bytes2int(StringUtil.hexStringToByte(strTxt));
        ActivityTPPaypass.m_PaypassParam = param;
       // Log.d("fanz", "savaParam: " + ActivityTPPaypass.m_PaypassParam.toString());
    }
}
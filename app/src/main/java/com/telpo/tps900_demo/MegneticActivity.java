package com.telpo.tps900_demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.magnetic.MagneticCard;

import airtel.com.kycfingerprint.fingerprint.FingerPrintDeviceInterface;
import airtel.com.kycfingerprint.fingerprint.FingerPrintManager;
import airtel.com.kycfingerprint.fingerprintDevices.FingerPrintDevices;

/**
 * For Magnetic stripe card test.
 * @author linhx
 * @date 2015-02-27
 *
 */
public class MegneticActivity extends Activity {
    private static final String TAG = "MegneticActivity";
    private EditText editText1,editText2,editText3;
	private Button click, initBtn;
	Handler handler;
	Thread readThread;
	TextView title_tv;
    private FingerPrintManager fingerPrintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.magnetic_main);
        title_tv=findViewById(R.id.title_tv);
        title_tv.setText("Magetic Card Test");
        editText1 = (EditText) findViewById(R.id.editText_track1);
        editText2 = (EditText) findViewById(R.id.editText_track2);
        editText3 = (EditText) findViewById(R.id.editText_track3);
        click = (Button) findViewById(R.id.button_open);
        initBtn = (Button) findViewById(R.id.button_quit);
//        initBtn.setEnabled(false);
//        handler = new Handler()
//        {
//
//			@Override
//			public void handleMessage(Message msg)
//			{
//                editText1.setText("");
//                editText2.setText("");
//                editText3.setText("");
//				String[] TracData = (String[])msg.obj;
//                for(int i=0; i<3; i++){
//                    if(TracData[i] != null){
//                        switch (i)
//                        {
//                            case 0:
//                                editText1.setText(TracData[i]);
//                                break;
//                            case 1:
//                                editText2.setText(TracData[i]);
//                                break;
//                            case 2:
//                                editText3.setText(TracData[i]);
//                                break;
//                        }
//
//                    }
//                }
//			}
//
//        };
        
        
//        try {
//            MagneticCard.open(MegneticActivity.this);
//        } catch (Exception e) {
//			click.setEnabled(false);
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setTitle(R.string.error);
//            alertDialog.setMessage(R.string.error_open_magnetic_card);
//            alertDialog.setPositiveButton(R.string.dialog_comfirm,new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    MegneticActivity.this.finish();
//                }
//            });
//            alertDialog.show();
//        }
//
//        click.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                editText1.setText("");
//                editText2.setText("");
//                editText3.setText("");
//				readThread = new ReadThread();
//				readThread.start();
//				click.setEnabled(false);
//				initBtn.setEnabled(true);
//            }
//        });
//
//        initBtn.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//            	readThread.interrupt();
//            	readThread = null;
//            	click.setEnabled(true);
//            	initBtn.setEnabled(false);
//            }
//        });

        initView();
    }


    private void initView(){
        click.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MegneticActivity.this,"click ",Toast.LENGTH_LONG).show();
                fingerPrintManager.startCapture(MegneticActivity.this);
            }
        });




        initBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(MegneticActivity.this,"initBtn ",Toast.LENGTH_LONG).show();
                button_onclick_fingerprintdevice(v);
            }
        });



        fingerPrintManager = FingerPrintManager.getInstance(this, FingerPrintDeviceInterface.BOTH,
                new FingerPrintManager.FingerPrintDeviceCallback() {
                    @Override
                    public void onPreviewImage(final Bitmap bitmap) {

                    }

                    @Override
                    public void onImageDetailsCreated(Object object) {
                        Log.d(TAG, "onImageDetailsCreated  "+object);
                        Toast.makeText(MegneticActivity.this, "onImageDetailsCreated object ", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onDeviceAttached(FingerPrintDevices device) {
                        Log.d(TAG, "onDeviceAttached  ");
                        Toast.makeText(MegneticActivity.this, "onDeviceAttached", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onDeviceDetached(FingerPrintDevices device) {

                    }

                    @Override
                    public void onPermissionChanged(boolean status) {

                    }

                    @Override
                    public void onDeviceInitialized() {
                        Log.d(TAG, "onDeviceInitialized  ");
                        Toast.makeText(MegneticActivity.this, "onDeviceInitialized", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onBluetoothFingerPrintDeviceSearchFailed() {

                    }

                    @Override
                    public void onBluetoothSearchingStarted() {

                    }

                    @Override
                    public void onBluetoothSearchingFinished() {

                    }

                    @Override
                    public void onBluetoothPairingStarted() {

                    }

                    @Override
                    public void onBluetoothPairingFinished() {

                    }

                    @Override
                    public void onBluetoothPairingFailed() {
                        Toast.makeText(MegneticActivity.this, "dd", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorText) {
                        Log.d(TAG, "onError  "+errorText);
                        Toast.makeText(MegneticActivity.this, errorText, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public FingerPrintManager.BluetoothFingerPrintCallBack getBluetoothImp() {
                        return null;
                    }
                });
    }



    public void button_onclick_fingerprintdevice(View view) {

        fingerPrintManager.initializeFingerPrintDevice();

    }




    protected void onDestroy() {
    	
    	if (readThread != null)
    	{
    		readThread.interrupt();
    	}
    	MagneticCard.close();
        super.onDestroy();
        
    }
    
    private class ReadThread extends Thread
    {
    	String[] TracData = null;
    	
		@Override
		public void run()
		{
			MagneticCard.startReading();
			while (!Thread.interrupted()){
				try{
					TracData = MagneticCard.check(1000);
					handler.sendMessage(handler.obtainMessage(1, TracData));
					MagneticCard.startReading();
				}catch (TelpoException e){
				}
			}
		}
    	
    }

}

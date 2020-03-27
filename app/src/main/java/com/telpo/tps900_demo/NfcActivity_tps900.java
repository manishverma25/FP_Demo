package com.telpo.tps900_demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;
import com.telpo.tps550.api.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NfcActivity_tps900 extends Activity {
	private final String TAG = "NfcActivity_tps900";
	private Button open_btn = null;
	private Button close_btn = null;
	private Button check_btn = null;
	private EditText uid_editText = null;
	
	private Button getAtsBtn = null;
	private TextView textViewAtsData = null;
	
	private EditText apud_editText = null;
	private Button sendApduBtn = null;
	private TextView textNfcReader = null;
	
	private Button authenticateBtn = null;
	private EditText blockEditText = null;
	private Button writeBlockBtn   = null;
	private Button readBlockBtn    = null;
	private TextView textViewBlockData = null;
	
	private EditText valueEditText = null;
	private Button writeValueBtn   = null;
	private Button readValueBtn    = null;
	private TextView textViewValueData = null;
	
	private Button incBtn = null;
	private Button decBtn = null;

	SimpleDateFormat formatter;
	Date curDate;
	Thread readThread;
	Handler handler;
	private final int CHECK_NFC_TIMEOUT = 1;
	private final int SHOW_NFC_DATA     = 2;
	private byte blockNum_1 = 1;
	private byte blockNum_2 = 2;
	private final byte B_CPU = 3;
	private final byte A_CPU = 1;
	private final byte A_M1  = 2;
	
	private final byte SRC_ADDR  = 2;
	private final byte DEST_ADDR = 2;
	
	Nfc nfc = new Nfc(this);
	OnClickListener listener;
	long time1,time2;
	TextView title_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.nfc_main);
		title_tv=findViewById(R.id.title_tv);
		title_tv.setText("NFC Test");
        listener = new OnClickListener() {
        	@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.nfc_open_btn:
						try {
							nfc.open();
						} catch (TelpoException e) {
							e.printStackTrace();
						}
						open_btn.setEnabled(false);
						close_btn.setEnabled(true);
						check_btn.setEnabled(true);
						break;
						
					case R.id.nfc_check_btn:
						readThread = new ReadThread();
						readThread.start();
						open_btn.setEnabled(false);
						check_btn.setEnabled(false);
						close_btn.setEnabled(true);
						break;
					
					case R.id.getAtsBtn:
						getAtsData();
						break;
						
					case R.id.buttonNfcAPDU:
						sendAPDUData();
						break;
						
					case R.id.authenticateBtn:
						m1CardAuthenticate();
						break;
						
					case R.id.writeBlockBtn:
						writeBlockData();
						break;
						
					case R.id.readBlockBtn:
						readBlockData();
						break;
					
					case R.id.writeValueBtn:
						writeValueData();
						break;
						
					case R.id.readValueBtn:
						readValueData();
						break;
						
					case R.id.incBtn:
						m1IncOperation();
						break;
						
					case R.id.decBtn:
						m1DecOperation();
						break;

						
					case R.id.nfc_close_btn:
						try {
							nfc.close();
						} catch (TelpoException e) {
							e.printStackTrace();
						}
						open_btn.setEnabled(true);						
						check_btn.setEnabled(false);
						getAtsBtn.setEnabled(false);
						sendApduBtn.setEnabled(false);
						authenticateBtn.setEnabled(false);
						writeBlockBtn.setEnabled(false);
						readBlockBtn.setEnabled(false);
						writeValueBtn.setEnabled(false);
						readValueBtn.setEnabled(false);
						incBtn.setEnabled(false);
						decBtn.setEnabled(false);
						close_btn.setEnabled(false);
						uid_editText.setText("");
						textNfcReader.setText("");
						textViewAtsData.setText("");
						textViewBlockData.setText("");
						textViewValueData.setText("");
						break;
						
					default:
						break;
				}
			}
		};  
        
        open_btn  = (Button) findViewById(R.id.nfc_open_btn);
        open_btn.setOnClickListener(listener);
        close_btn = (Button) findViewById(R.id.nfc_close_btn);
        close_btn.setOnClickListener(listener);
        check_btn = (Button) findViewById(R.id.nfc_check_btn);
        check_btn.setOnClickListener(listener);
        uid_editText  = (EditText) findViewById(R.id.nfc_uid_data);
        
        getAtsBtn = (Button) findViewById(R.id.getAtsBtn);
        getAtsBtn.setOnClickListener(listener);
        textViewAtsData = (TextView) findViewById(R.id.textViewAtsData);
        apud_editText = (EditText)findViewById(R.id.editTextNfcAPDU);
        sendApduBtn   = (Button)findViewById(R.id.buttonNfcAPDU);
        sendApduBtn.setOnClickListener(listener);
        textNfcReader = (TextView)findViewById(R.id.textNfcReader);
        
        authenticateBtn = (Button) findViewById(R.id.authenticateBtn);
        authenticateBtn.setOnClickListener(listener);
        blockEditText = (EditText) findViewById(R.id.editTexWriteBlock);
        writeBlockBtn = (Button) findViewById(R.id.writeBlockBtn);
        writeBlockBtn.setOnClickListener(listener);
        readBlockBtn  = (Button) findViewById(R.id.readBlockBtn);
        readBlockBtn.setOnClickListener(listener);
        textViewBlockData = (TextView) findViewById(R.id.textViewBlockData);
        
        valueEditText = (EditText) findViewById(R.id.editTexWriteValue);
        writeValueBtn = (Button) findViewById(R.id.writeValueBtn);
        writeValueBtn.setOnClickListener(listener);
        readValueBtn  = (Button) findViewById(R.id.readValueBtn);
        readValueBtn.setOnClickListener(listener);
        textViewValueData = (TextView) findViewById(R.id.textViewValueData);
        
        incBtn        = (Button) findViewById(R.id.incBtn);
        incBtn.setOnClickListener(listener);
        decBtn        = (Button) findViewById(R.id.decBtn);
        decBtn.setOnClickListener(listener);

        
        open_btn.setEnabled(true);
		check_btn.setEnabled(false);
		getAtsBtn.setEnabled(false);
		sendApduBtn.setEnabled(false);
		authenticateBtn.setEnabled(false);
		writeBlockBtn.setEnabled(false);
		readBlockBtn.setEnabled(false);
		writeValueBtn.setEnabled(false);
		readValueBtn.setEnabled(false);
		incBtn.setEnabled(false);
		decBtn.setEnabled(false);
		close_btn.setEnabled(false);
		
        handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case CHECK_NFC_TIMEOUT: {
						Toast.makeText(NfcActivity_tps900.this, "Check card time out!", Toast.LENGTH_LONG).show();
						open_btn.setEnabled(true);
						close_btn.setEnabled(false);
						check_btn.setEnabled(false);
					}break;
					case SHOW_NFC_DATA:{
						byte[] uid_data = (byte[]) msg.obj;
						if (uid_data[0] == 0x42) {
							// TYPE B类（暂时只支持cpu卡）	
							byte[] atqb = new byte[uid_data[16]];
							byte[] pupi = new byte[4];
							String type = null;
							
							System.arraycopy(uid_data, 17, atqb, 0, uid_data[16]);
							System.arraycopy(uid_data, 29, pupi, 0, 4);
							
							if (uid_data[1] == B_CPU) {
								type = "CPU";
								sendApduBtn.setEnabled(true);
								getAtsBtn.setEnabled(true);
							} else {
								type = "unknow";
							}
							
							uid_editText.setText(getString(R.string.card_type) + getString(R.string.type_b) + " " + type +
									"\r\n" + getString(R.string.atqb_data) + StringUtil.toHexString(atqb) +
									"\r\n" + getString(R.string.pupi_data) + StringUtil.toHexString(pupi));
							
						} else if (uid_data[0] == 0x41) {
							// TYPE A类（CPU, M1）
							byte[] atqa = new byte[2];
							byte[] sak = new byte[1];
							byte[] uid = new byte[uid_data[5]];
							String type = null;
							
							System.arraycopy(uid_data, 2, atqa, 0, 2);
							System.arraycopy(uid_data, 4, sak, 0, 1);
							System.arraycopy(uid_data, 6, uid, 0, uid_data[5]);
							
							if (uid_data[1] == A_CPU) {
								type = "CPU";
								sendApduBtn.setEnabled(true);
								getAtsBtn.setEnabled(true);
							} else if (uid_data[1] == A_M1) {
								type = "M1";
								authenticateBtn.setEnabled(true);
							} else {
								type = "unknow";
							}
							
							uid_editText.setText(getString(R.string.card_type) + getString(R.string.type_a) + " " + type +
									"\r\n" + getString(R.string.atqa_data) + StringUtil.toHexString(atqa) +
									"\r\n" + getString(R.string.sak_data) + StringUtil.toHexString(sak) +
									"\r\n" + getString(R.string.uid_data) + StringUtil.toHexString(uid));
						} else {
							Log.e(TAG, "unknow type card!!");
						}
					}break;
					
					default:break;
				}
			}	
        };
    }
    
    @Override
    protected void onDestroy() {
    	try {
    		nfc.close();
		} catch (TelpoException e) {
			e.printStackTrace();
		}
    	super.onDestroy();
    }
    
    private class ReadThread extends Thread {
    	byte[] nfcData = null;
    	
		@Override
		public void run() {
			try {

				time1= System.currentTimeMillis();
				nfcData = nfc.activate(10 * 1000); // 10s
				time2= System.currentTimeMillis();
				Log.e("yw activate",(time2-time1)+"");
				if (null != nfcData) {
					handler.sendMessage(handler.obtainMessage(SHOW_NFC_DATA, nfcData));
				} else {
					Log.d(TAG, "Check Card timeout...");
					handler.sendMessage(handler.obtainMessage(CHECK_NFC_TIMEOUT, null));
				}
			} catch (TelpoException e) {
				Log.e("yw",e.toString());
				e.printStackTrace();
			}
		}
    }
    
    public void sendAPDUData() {
		byte[] pSendAPDU;
		byte[] result = null;
		String apduStr;
		int iRet = 0;
		int length = 0;
		
		Log.d("sendAPDUkOnClick", "sendAPDUkOnClick");
		apduStr = apud_editText.getText().toString();
		pSendAPDU = toByteArray(apduStr);
		length = pSendAPDU.length;
		try {
			result = nfc.transmit(pSendAPDU, length);
		} catch (TelpoException e) {
			e.printStackTrace();
		}
		
		textNfcReader.setText(TextUtils.isEmpty(StringUtil.toHexString(result)) ? getString(R.string.send_APDU_fail) : getString(R.string.send_APDU_success) + StringUtil.toHexString(result));
		if (!TextUtils.isEmpty(StringUtil.toHexString(result))) {
			Toast.makeText(NfcActivity_tps900.this,
					getString(R.string.send_comm_success), Toast.LENGTH_SHORT).show();
		}
	}
    
    public static byte[] toByteArray(String hexString) {
		int hexStringLength = hexString.length();
		byte[] byteArray = null;
		int count = 0;
		char c;
		int i;

		// Count number of hex characters
		for (i = 0; i < hexStringLength; i++) {
			c = hexString.charAt(i);
			if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
				count++;
			}
		}

		byteArray = new byte[(count + 1) / 2];
		boolean first = true;
		int len = 0;
		int value;
		for (i = 0; i < hexStringLength; i++) {
			c = hexString.charAt(i);
			if (c >= '0' && c <= '9') {
				value = c - '0';
			} else if (c >= 'A' && c <= 'F') {
				value = c - 'A' + 10;
			} else if (c >= 'a' && c <= 'f') {
				value = c - 'a' + 10;
			} else {
				value = -1;
			}

			if (value >= 0) {

				if (first) {
					byteArray[len] = (byte) (value << 4);
				} else {
					byteArray[len] |= value;
					len++;
				}
				first = !first;
			}
		}
		return byteArray;
	}
    	
	public void writeBlockData() {
		byte[] blockData = null;
		String blockStr;
		Boolean status = true;
		
		Log.d(TAG, "writeBlockData...");
		blockStr = blockEditText.getText().toString();
		blockData = toByteArray(blockStr);
		
		try {
			nfc.m1_write_block(blockNum_1, blockData, blockData.length);
		} catch (TelpoException e) {
			status = false;
			Log.e("yw",e.toString());
			e.printStackTrace();
		}
		
		if (status) {
			Log.d(TAG, "writeBlockData success!");
			Toast.makeText(this, getString(R.string.operation_succss), Toast.LENGTH_SHORT).show();
		} else {
			Log.e(TAG, "writeBlockData fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void readBlockData() {
		byte[] data = null;
		try {

			time1= System.currentTimeMillis();
			data = nfc.m1_read_block(blockNum_1);
			time2= System.currentTimeMillis();
			Log.e("yw read_block",(time2-time1)+"");




		} catch (TelpoException e) {
			e.printStackTrace();
		}
		
		if (data == null) {
			Log.e(TAG, "readBlockBtn fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		} else {
			textViewBlockData.setText(StringUtil.toHexString(data));
		}
	}
	
	public void writeValueData() {
		byte[] valueData = null;
		String valueStr;
		boolean status = true;
		
		Log.d(TAG, "writeValueBtn...");
		valueStr = valueEditText.getText().toString();
		valueData = toByteArray(valueStr);
		
		try {
			nfc.m1_write_value(blockNum_2, valueData, valueData.length);
		} catch (TelpoException e) {
			status = false;
			e.printStackTrace();
		}
		
		if (status) {
			Log.d(TAG, "writeValueData success!");
			Toast.makeText(this, getString(R.string.operation_succss), Toast.LENGTH_SHORT).show();
		} else {
			Log.e(TAG, "writeValueData fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		}	
	}
	
	public void readValueData() {
		byte[] data = null;
		try {
			data = nfc.m1_read_value(blockNum_2);
		} catch (TelpoException e) {
			e.printStackTrace();
		}
		
		if (null == data) {
			Log.e(TAG, "readValueBtn fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		} else {
			textViewValueData.setText(StringUtil.toHexString(data));
		}
	}
	
	public void getAtsData() {
		byte[] data = null;
		Boolean status = true;
		try {
			data = nfc.cpu_get_ats();
		} catch (TelpoException e) {
			status = false;
			e.printStackTrace();
		}
		
		if (status) {
			if (data == null) {
				textViewAtsData.setText("null");
			} else  {
				textViewAtsData.setText(StringUtil.toHexString(data));
			}
		} else {
			Log.e(TAG, "getAtsData fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void m1CardAuthenticate() {
		Boolean status = true;
		byte[] passwd={(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
		try {

			time1= System.currentTimeMillis();
			nfc.m1_authenticate(blockNum_1, (byte)0x0A, passwd);
			time2= System.currentTimeMillis();
			Log.e("yw m1_authenticate",(time2-time1)+"");



		} catch (TelpoException e) {
			status = false;
			e.printStackTrace();
			Log.e("yw",e.toString());
		}
		
		if (status) {
			Log.d(TAG, "m1CardAuthenticate success!");
			authenticateBtn.setEnabled(false);
			writeBlockBtn.setEnabled(true);
			readBlockBtn.setEnabled(true);
			writeValueBtn.setEnabled(true);
			readValueBtn.setEnabled(true);
			incBtn.setEnabled(true);
			decBtn.setEnabled(true);
		} else {
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
			Log.d(TAG, "m1CardAuthenticate fail!");
		}
	}
	
	public void m1IncOperation() {
		Boolean status = true;
		byte[] data = new byte[4];
		data[0] = 0x01;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		
		try {
			nfc.m1_increment(SRC_ADDR, DEST_ADDR, data, 4);
		} catch (TelpoException e) {
			status = false;
			e.printStackTrace();	
		}
		
		if (status) {
			Log.d(TAG, "m1IncOperation success!");
			Toast.makeText(this, getString(R.string.operation_succss), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
			Log.d(TAG, "m1IncOperation fail!");
		}
	}
	
	public void m1DecOperation() {
		Boolean status = true;
		byte[] data = new byte[4];
		data[0] = 0x01;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		
		try {
			nfc.m1_decrement(SRC_ADDR, DEST_ADDR, data, 4);
		} catch (TelpoException e) {
			status = false;
			e.printStackTrace();
		}
		
		if (status) {
			Log.d(TAG, "m1DecOperation success!");
			Toast.makeText(this, getString(R.string.open_success), Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "m1DecOperation fail!");
			Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
		}
	}

}

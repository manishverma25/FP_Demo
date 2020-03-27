package com.telpo.tps900_demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.telpo.tps550.api.reader.SmartCardReader;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

public class PsamActivity extends Activity
{
	private SmartCardReader reader;
	private Button readButton, protocolbtn, buttonAPDU;
	private Button poweronButton;
	private Button poweroffButton;
	private Button openButton;
	private Button closeButton;
	private EditText mEditTextApdu;
	private TextView textReader;
	TextView title_tv;
	private Button psam1;
	private Button psam2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.smartcard);
		title_tv=findViewById(R.id.title_tv);
		title_tv.setText("PSAM Test");
		mEditTextApdu = (EditText) findViewById(R.id.editTextAPDU);
		textReader = (TextView) findViewById(R.id.textReader);
		psam1 = (Button) findViewById(R.id.psam1);
		psam2 = (Button) findViewById(R.id.psam2);

		readButton = (Button)findViewById(R.id.read_btn);
		readButton.setOnClickListener(listener);
		readButton.setEnabled(false);
		poweronButton = (Button)findViewById(R.id.poweron_btn);
		poweronButton.setOnClickListener(listener);
		poweronButton.setEnabled(false);
		poweroffButton = (Button)findViewById(R.id.poweroff_btn);
		poweroffButton.setOnClickListener(listener);
		poweroffButton.setEnabled(false);
		openButton = (Button)findViewById(R.id.open_btn);
		openButton.setOnClickListener(listener);
		closeButton = (Button)findViewById(R.id.close_btn);
		closeButton.setOnClickListener(listener);
		closeButton.setEnabled(false);
		protocolbtn = (Button) findViewById(R.id.protocol_btn);
		protocolbtn.setOnClickListener(listener);
		protocolbtn.setEnabled(false);
		buttonAPDU = (Button) findViewById(R.id.buttonAPDU);
		buttonAPDU.setEnabled(false);


		reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM1);
	}

	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.open_btn:
					new openTask().execute();
					break;

				case R.id.close_btn:
					reader.close();
					closeButton.setEnabled(false);
					openButton.setEnabled(true);
					poweroffButton.setEnabled(false);
					poweronButton.setEnabled(false);
					readButton.setEnabled(false);
					protocolbtn.setEnabled(false);
					buttonAPDU.setEnabled(false);
					textReader.setText("");
					break;

				case R.id.read_btn:
					String atrString = reader.getATRString();
					textReader.setText("ATR:" + (TextUtils.isEmpty(atrString.trim()) ? "null" : atrString));
					Toast.makeText(PsamActivity.this, getString(R.string.get_data_success), Toast.LENGTH_SHORT).show();
					break;

				case R.id.poweron_btn:
					if (reader.iccPowerOn()) {
						poweronButton.setEnabled(false);
						poweroffButton.setEnabled(true);
						readButton.setEnabled(true);
						protocolbtn.setEnabled(true);
						buttonAPDU.setEnabled(true);
					} else {
						Toast.makeText(PsamActivity.this, "ICC power on failed", Toast.LENGTH_SHORT).show();
					}

					break;

				case R.id.poweroff_btn:
					reader.iccPowerOff();
					poweroffButton.setEnabled(false);
					poweronButton.setEnabled(true);
					readButton.setEnabled(false);
					protocolbtn.setEnabled(false);
					buttonAPDU.setEnabled(false);
					break;
				case R.id.protocol_btn:
					int proto = reader.getProtocol();
					if (proto == SmartCardReader.PROTOCOL_T0) {
						textReader.setText("protocol: T0");
					} else if (proto == SmartCardReader.PROTOCOL_T1) {
						textReader.setText("protocol: T1");
					} else {
						textReader.setText("protocol: NA");
					}
					Toast.makeText(PsamActivity.this, getString(R.string.get_protocol_success), Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
	};

	public void changePsam(View view) {
		switch (view.getId()) {
			case R.id.psam1:
				psam1.setEnabled(false);
				psam2.setEnabled(true);
				if (reader.close()) {
					reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM1);
					closeButton.setEnabled(false);
					openButton.setEnabled(true);
					poweroffButton.setEnabled(false);
					poweronButton.setEnabled(false);
					readButton.setEnabled(false);
					protocolbtn.setEnabled(false);
					buttonAPDU.setEnabled(false);
					textReader.setText("");
				}

				break;

			case R.id.psam2:
				psam1.setEnabled(true);
				psam2.setEnabled(false);
				if (reader.close()) {
					reader = new SmartCardReader(PsamActivity.this, SmartCardReader.SLOT_PSAM2);
					closeButton.setEnabled(false);
					openButton.setEnabled(true);
					poweroffButton.setEnabled(false);
					poweronButton.setEnabled(false);
					readButton.setEnabled(false);
					protocolbtn.setEnabled(false);
					buttonAPDU.setEnabled(false);
					textReader.setText("");
				}

				break;

		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal())
			psam2.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private class openTask extends AsyncTask<Void, Integer, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... params)
		{
			Log.d("idcard demo", "psam open task");
			return reader.open();
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result)
			{
				openButton.setEnabled(false);
				closeButton.setEnabled(true);
				poweronButton.setEnabled(true);
			}
			else
			{
				Toast.makeText(PsamActivity.this, "Open reader failed", Toast.LENGTH_SHORT).show();
			}
		}
	}


	public void sendAPDUkOnClick(View view) {
		byte[] pSendAPDU;
		byte[] result;
		int[] pRevAPDULen = new int[1];
		String apduStr;

		Log.d("sendAPDUkOnClick", "sendAPDUkOnClick");
		pRevAPDULen[0] = 300;
		apduStr = mEditTextApdu.getText().toString();
		pSendAPDU = toByteArray(apduStr);
		result = reader.transmit(pSendAPDU);
		textReader.setText(TextUtils.isEmpty(StringUtil.toHexString(result)) ? getString(R.string.send_APDU_fail) : getString(R.string.send_APDU_success) + StringUtil.toHexString(result));
		if (!TextUtils.isEmpty(StringUtil.toHexString(result))) {
			Toast.makeText(PsamActivity.this, getString(R.string.send_comm_success), Toast.LENGTH_SHORT).show();
		}

	}

	private String BCD2Str(byte[] data)
	{
		String string;
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < data.length; i++)
		{
			string = Integer.toHexString(data[i] & 0xFF);
			if (string.length() == 1)
			{
				stringBuilder.append("0");
			}

			stringBuilder.append(string.toUpperCase());
			stringBuilder.append(" ");
		}

		return stringBuilder.toString();
	}

	private byte[] str2BCD(String string)
	{
		int len;
		String str;
		String hexStr = "0123456789ABCDEF";

		String s = string.toUpperCase();

		len = s.length();
		if ((len % 2) == 1)
		{
			// 长度不为偶数，右补0
			str = s + "0";
			len = (len + 1) >> 1;
		}
		else
		{
			str = s;
			len >>= 1;
		}

		byte[] bytes = new byte[len];
		byte high;
		byte low;

		for (int i = 0, j = 0; i < len; i++, j += 2)
		{
			high = (byte)(hexStr.indexOf(str.charAt(j)) << 4);
			low = (byte)hexStr.indexOf(str.charAt(j + 1));
			bytes[i] = (byte)(high | low);
		}

		return bytes;
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

}

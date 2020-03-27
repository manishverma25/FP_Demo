package airtel.com.kycfingerprint.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import airtel.com.kycfingerprint.utility.ADLogger;;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@SuppressLint("NewApi")
public class BluetoothComm {
    /**
     * Service UUID
     */
    public final static String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";
    /**
     * Bluetooth address code
     */
    private String msMAC;
    /**
     * Bluetooth connection status
     */
    private boolean mbConectOk = false;

    /* Get Default Adapter */
    private BluetoothAdapter mBT = BluetoothAdapter.getDefaultAdapter();
    /**
     * Bluetooth serial port connection object
     */
    private BluetoothSocket mbsSocket = null;
    /**
     * Input stream object
     */
    public static InputStream misIn = null;
    /**
     * Output stream object
     */
    public static OutputStream mosOut = null;
    /**
     * Constant: The current Adnroid SDK version number
     */
    private static final int SDK_VER;

    static {
        SDK_VER = Build.VERSION.SDK_INT;
    }

    ;

    /**
     * Constructor
     *
     * @param sMAC Bluetooth device MAC address required to connect
     */
    public BluetoothComm(String sMAC) {
        this.msMAC = sMAC;
    }


    /**
     * Disconnect the Bluetooth device connection
     *
     * @return void
     */
    public void closeConn() {
        if (this.mbConectOk) {
            try {
                if (null != this.misIn)
                    this.misIn.close();
                if (null != this.mosOut)
                    this.mosOut.close();
                if (null != this.mbsSocket)
                    this.mbsSocket.close();
                this.mbConectOk = false;//Mark the connection has been closed
            } catch (IOException e) {
                //Any part of the error, will be forced to close socket connection
                this.misIn = null;
                this.mosOut = null;
                this.mbsSocket = null;
                this.mbConectOk = false;//Mark the connection has been closed
            }
        }
        Log.e(TAG, " Closed connection");
    }

    private static final String TAG = "Prowess BT Comm";

    /**
     * Bluetooth devices establish serial communication connection <br />
     * This function is best to put the thread to call, because it will block the system when calling
     *
     * @return Boolean false: connection creation failed / true: the connection is created successfully
     */
    final public boolean createConn() {
        if (!mBT.isEnabled())
            return false;
        Log.e(TAG, ".....crete connection  1");
        //If a connection already exists, disconnect
        if (mbConectOk)
            this.closeConn();
        Log.e(TAG, ".....crete connection  1");
        /*Start Connecting a Bluetooth device*/
        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.msMAC);
        final UUID uuidComm = UUID.fromString(UUID_STR);
        try {
            ADLogger.d(TAG, "..... createRfcommSocketToServiceRecord  1");
            this.mbsSocket = device.createRfcommSocketToServiceRecord(uuidComm);
            Thread.sleep(2000);
            Log.e(TAG, ">>> Connecting ");
            this.mbsSocket.connect();
            Log.e(TAG, ">>> CONNECTED SUCCESSFULLY 1 ");
            Thread.sleep(2000);
            this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
            this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
            this.mbConectOk = true; //Device is connected successfully

        } catch (Exception e) {
            try {
                Thread.sleep(2000);
                Log.e(TAG, ">>>>>>           Try 2  ................!");
                ADLogger.d(TAG, "..... createInsecureRfcommSocketToServiceRecord  2");
                this.mbsSocket = device.createInsecureRfcommSocketToServiceRecord(uuidComm);
                Log.e(TAG, " Socket obtained");
                Thread.sleep(2000);
                Log.e(TAG, " Connecting again ");
                this.mbsSocket.connect();
                Log.e(TAG, " Successful connection 2nd time....... ");
                Thread.sleep(2000);
                this.mosOut = this.mbsSocket.getOutputStream();//Get global output stream object
                this.misIn = this.mbsSocket.getInputStream(); //Get global streaming input object
                this.mbConectOk = true;
            } catch (IOException e1) {
                Log.e(TAG, " Connection Failed by trying both ways....... ");
                e1.printStackTrace();
                this.closeConn();//Disconnect
                Log.e(TAG, " Returning False");
                return false;
            } catch (Exception ee) {
                Log.e(TAG, " Connection Failed due to other reasons....... ");
                ee.printStackTrace();
                this.closeConn();//Disconnect
                Log.e(TAG, " Returning False");
                return false;
            }
        }
        return true;
    }

    /**
     * If the communication device has been established
     *
     * @return Boolean true: communication has been established / false: communication lost
     */
    public boolean isConnect() {
        return this.mbConectOk;
    }


}

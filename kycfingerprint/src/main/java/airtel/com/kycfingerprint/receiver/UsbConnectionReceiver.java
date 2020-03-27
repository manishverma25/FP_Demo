package airtel.com.kycfingerprint.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.RequiresApi;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import airtel.com.kycfingerprint.utility.ADLogger;

/**
 * Created by A1P5KF3Z on 3/23/17.
 */

public class UsbConnectionReceiver extends BroadcastReceiver {

    public static final String ACTION_USB_PERMISSION =
            "airtel.com.fingerprintdemo.USB_PERMISSION";
    public static final String ACTION_STARTEK_PERMISSION =
            "com.ACPL.FM220_Telecom.USB_PERMISSION";
    private static final String TAG = UsbConnectionReceiver.class.getSimpleName();
    private static UsbConnectionCallback usbConnectionCallback;
    private final Context context;

    public UsbConnectionReceiver(Context context, UsbConnectionCallback callback) {
        this.context = context;
        usbConnectionCallback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//            Toast.makeText(context,
//                    "ACTION_USB_DEVICE_ATTACHED: \n", Toast.LENGTH_SHORT).show();
            UsbDevice deviceFound = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (usbConnectionCallback != null) {
                usbConnectionCallback.onUSBDeviceAttached(deviceFound);
            }

        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//            Toast.makeText(context,
//                    "ACTION_USB_DEVICE_DETACHED: \n", Toast.LENGTH_SHORT).show();
            if (usbConnectionCallback != null) {
                usbConnectionCallback.onUSBDeviceDetached(device);
            }
        } else if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                final UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                    Toast.makeText(context,
//                            "ACTION_USB_PERMISSION_GRANTED: \n", Toast.LENGTH_SHORT).show();
                    if (usbConnectionCallback != null)
                        usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(device, true);
                } else {
//                    Toast.makeText(context,
//                            "ACTION_USB_PERMISSION_DECLINED: \n", Toast.LENGTH_SHORT).show();
                    if (usbConnectionCallback != null)
                        usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(device, false);
                }
            }
        } else if (ACTION_STARTEK_PERMISSION.equals(action)) {

            UsbDevice deviceFound = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (deviceFound != null) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                    Toast.makeText(context,
//                            "STARTEK_PERMISSION_GRANTED: \n", Toast.LENGTH_SHORT).show();
                    if (usbConnectionCallback != null)
                        usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(deviceFound, true);
                } else {
//                    Toast.makeText(context,
//                            "STARTEK_PERMISSION_DECLINED: \n", Toast.LENGTH_SHORT).show();
                    if (usbConnectionCallback != null)
                        usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(deviceFound, false);
                }
            }
//        } else if (FamocoInitializer.ACTION_USB_PERMISSION.equals(intent.getAction())) {
//            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                ADLogger.e(TAG, "\t --> Permission granted for device" + device);
//                if (device != null) {
//                    ADLogger.e(TAG, "\t --> Device USB found ");
//                    // Initiate if permission granted
//                    if (usbConnectionCallback != null)
//                        usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(device, true);
//                }
//            } else {
//                ADLogger.e(TAG, "\t --> Permission not granted for device" + device);
//                if (usbConnectionCallback != null)
//                    usbConnectionCallback.onUSBDeviceAttachedPermissionChanged(device, false);
//            }
        }
    }

    public void fetchConnectedDevices(final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public void run() {
                UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                ArrayList<UsbDevice> usbDevicesList = null;
                if (usbDevicesList == null)
                    usbDevicesList = new ArrayList<>();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    usbDevicesList.add(device);
                }
                if (usbConnectionCallback != null && usbDevicesList.size() > 0) {
                    final ArrayList<UsbDevice> finalUsbDevicesList = usbDevicesList;
//                    Toast.makeText(context, "FETCHED CONNECTED DEVICES | " + finalUsbDevicesList.size(), Toast.LENGTH_SHORT).show();
                    usbConnectionCallback.connectedUsbDeviceFound(usbDevicesList);
                }
            }
        });
    }

    public interface UsbConnectionCallback {
        void connectedUsbDeviceFound(ArrayList<UsbDevice> usbDevices);

        void onUSBDeviceAttached(UsbDevice usbDevice);

        void onUSBDeviceDetached(UsbDevice usbDevice);

        void onUSBDeviceAttachedPermissionChanged(UsbDevice usbDevice, boolean isPermissionGranted);
    }
}




package airtel.com.kycfingerprint.fingerprint;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import airtel.com.kycfingerprint.bluetooth.BluetoothComm;
import airtel.com.kycfingerprint.bluetooth.BluetoothPair;
import airtel.com.kycfingerprint.fingerprintCallback.FingerPrintCallback;
import airtel.com.kycfingerprint.fingerprintDevices.FingerPrintDevices;
import airtel.com.kycfingerprint.receiver.BluetoothReceiver;
import airtel.com.kycfingerprint.receiver.UsbConnectionReceiver;

//import airtel.com.kycfingerprint.fingerprintDevices.MantraInitializer;

/**
 * Created by A1P5KF3Z on 1/12/17.
 */

public class FingerPrintManager implements FingerPrintCallback {

    //    public static final String EVOLUTE_BLUETOOTH_DEVICE_NAME = "ESIAA0120";
    public static final String EVOLUTE_BLUETOOTH_DEVICE_NAME_PREFIX = "ESI";
    private static FingerPrintManager fingerPrintManager;
    private Context context;
    //    private Handler mainThreadHandler;
    private FingerPrintDeviceCallback fingerPrintDeviceCallback;
    private FingerPrintInitWrapper fingerPrintInitWrapper;
    private UsbConnectionReceiver usbConnectionReceiver;
    private BluetoothReceiver bluetoothReceiver;
    private PendingIntent mPermissionIntent;
    private FingerPrintDevices currentFingerPrintDevice;
    private FingerPrintDeviceInterface fingerPrintDeviceInterface;
    private BluetoothDevice discoveredBluetoothDevice;
    private boolean isBluetoothConnectionInProgress;

    private FingerPrintManager(final Context context, final FingerPrintDeviceCallback fingerPrintDeviceCallback, FingerPrintDeviceInterface fingerPrintDeviceInterface) {
        this.context = context;
        this.fingerPrintDeviceCallback = fingerPrintDeviceCallback;
        this.fingerPrintDeviceInterface = fingerPrintDeviceInterface;
    }

    public static FingerPrintManager getInstance(Context context, FingerPrintDeviceInterface fingerPrintDeviceInterface, FingerPrintDeviceCallback fingerPrintDeviceCallback) {
        if (fingerPrintManager == null) {
            fingerPrintManager = new FingerPrintManager(context, fingerPrintDeviceCallback, fingerPrintDeviceInterface);
        } else {
            fingerPrintManager.context = context;
            fingerPrintManager.fingerPrintDeviceCallback = fingerPrintDeviceCallback;
            fingerPrintManager.fingerPrintDeviceInterface = fingerPrintDeviceInterface;
        }
        fingerPrintManager.setupReceivers();
        return fingerPrintManager;
    }

    private void setupReceivers() {

        usbConnectionReceiver = new UsbConnectionReceiver(context, new UsbConnectionReceiver.UsbConnectionCallback() {
            @Override
            public void connectedUsbDeviceFound(ArrayList<UsbDevice> usbDevices) {
                FingerPrintDevices fingerPrintDevice = checkIfValidDevicesConnected(usbDevices);
//                if (fingerPrintDevice != null) {
//                    fingerPrintDeviceCallback.onDeviceAttached(fingerPrintDevice);
//                }
            }

            @Override
            public void onUSBDeviceAttached(UsbDevice usbDevice) {
                ArrayList<UsbDevice> usbDevices = new ArrayList();
                usbDevices.add(usbDevice);
                FingerPrintDevices fingerPrintDevice = checkIfValidDevicesConnected(usbDevices);
                if (fingerPrintDevice != null) {
                    fingerPrintDeviceCallback.onDeviceAttached(fingerPrintDevice);

                    if (fingerPrintInitWrapper == null && fingerPrintDevice == FingerPrintDevices.FAMOCO) {
                        autoInitializeFingerPrintDevice();
                    }
                }
            }

            @Override
            public void onUSBDeviceDetached(UsbDevice usbDevice) {
                FingerPrintDevices fingerPrintDevice = getDeviceByProductIDAndVendorID(usbDevice.getProductId(), usbDevice.getVendorId());
                if (fingerPrintDevice != null)
                    fingerPrintDeviceCallback.onDeviceDetached(fingerPrintDevice);
            }

            @Override
            public void onUSBDeviceAttachedPermissionChanged(UsbDevice usbDevice, boolean isPermissionGranted) {
                fingerPrintDeviceCallback.onPermissionChanged(isPermissionGranted);
                if (isPermissionGranted) {
                    currentFingerPrintDevice = getDeviceByProductIDAndVendorID(usbDevice.getProductId(), usbDevice.getVendorId());
                    autoInitializeFingerPrintDevice();
                }
            }
        });
        if (fingerPrintDeviceInterface == FingerPrintDeviceInterface.BLUETOOTH
                || fingerPrintDeviceInterface == FingerPrintDeviceInterface.BOTH) {
            bluetoothReceiver = new BluetoothReceiver(new BluetoothReceiver.BluetoothReceiverCallback() {

                @Override
                public void onBluetoothPairingChanged(BluetoothDevice device, boolean status) {
                    if (status == true && device.getName().toUpperCase().contains(EVOLUTE_BLUETOOTH_DEVICE_NAME_PREFIX)) {
                        currentFingerPrintDevice = FingerPrintDevices.EVOLUTE;
//                        fingerPrintDeviceCallback.onPermissionChanged(true);
//                        if (fingerPrintInitWrapper == null) {
//                            fingerPrintInitWrapper = new EvoluteInitializer(context, FingerPrintManager.this);
//                        }
                    }
//                    else {
//                        fingerPrintDeviceCallback.onPermissionChanged(false);
//                    }
                }

                @Override
                public void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice) {
                    if (bluetoothDevice.getName().toUpperCase().contains(EVOLUTE_BLUETOOTH_DEVICE_NAME_PREFIX)) {
                        discoveredBluetoothDevice = bluetoothDevice;
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        bluetoothAdapter.cancelDiscovery();
                    }
                }

                @Override
                public void onBluetoothSearchingStarted() {
                    fingerPrintDeviceCallback.onBluetoothSearchingStarted();
                }

                @Override
                public void onBluetoothSearchingFinished() {
                    fingerPrintDeviceCallback.onBluetoothSearchingFinished();
                    proceedForBluetoothPairing();
                }
            });
        }
//        fingerPrintInitWrapper = null;
        registerStartek();
//        registerFamoco();

        createPermission();
        usbConnectionReceiver.fetchConnectedDevices(context);
        createPermissionForBluetooth();
        if (fingerPrintDeviceInterface == FingerPrintDeviceInterface.BLUETOOTH)
            startBluetoothInitialization();
    }

    public void startBluetoothInitialization() {
        if (isBluetoothConnectionInProgress)
            return;
        if (discoveredBluetoothDevice != null && discoveredBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            fingerPrintDeviceCallback.onBluetoothPairingFinished();
            fingerPrintDeviceCallback.onDeviceInitialized();
            return;
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    bluetoothAdapter.startDiscovery();
                }
            }, 1000);
        }
    }

    private void proceedForBluetoothPairing() {
        if (discoveredBluetoothDevice != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fingerPrintDeviceCallback.onBluetoothPairingStarted();
                    /** discoveredBluetoothDevice.createBond() requires  android os >=  Build.VERSION_CODES.KITKAT**/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        discoveredBluetoothDevice.createBond();
                    }
                    BluetoothComm bluetoothComm = new BluetoothComm(discoveredBluetoothDevice.getAddress());
                    bluetoothComm.createConn();
                    if (discoveredBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        fingerPrintDeviceCallback.onBluetoothPairingFinished();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (fingerPrintInitWrapper == null)
//                                            fingerPrintInitWrapper = new EvoluteInitializer(context, FingerPrintManager.this);
                                        isBluetoothConnectionInProgress = false;
                                    }
                                });
                            }
                        }, 500);
                    } else {
                        fingerPrintDeviceCallback.onBluetoothPairingFailed();
                        isBluetoothConnectionInProgress = false;
                    }
                }
            }).start();
        } else {
            fingerPrintDeviceCallback.onBluetoothFingerPrintDeviceSearchFailed();
            isBluetoothConnectionInProgress = false;
        }
    }

    void initDeviceObject() {
//        fingerPrintInitWrapper = new Mantra200Initializer(context, this);
//        if (currentFingerPrintDevice == FingerPrintDevices.MANTRA)
//            fingerPrintInitWrapper = new Mantra100Initializer(context, this);

//          if (currentFingerPrintDevice == FingerPrintDevices.STARTEC)
//            fingerPrintInitWrapper = new StartekInitializer(context, this);

//        if (currentFingerPrintDevice == FingerPrintDevices.MANTRA_200)
//            fingerPrintInitWrapper = new Mantra200Initializer(context, this);

    }

    public void initializeFingerPrintDevice() {
        fingerPrintInitWrapper.initialize();
        callTimerForCallback();
    }

    public void startCapture() {
        if (fingerPrintInitWrapper != null)
            fingerPrintInitWrapper.startCapture();
    }

    public void startCapture(final Context context) {

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (fingerPrintInitWrapper != null) {
                    fingerPrintInitWrapper.updateContext(context);
                    fingerPrintInitWrapper.startCapture();
                }
            }
        };

        if (fingerPrintInitWrapper == null) {
            autoInitializeFingerPrintDevice();
            final ProgressDialog dialog = ProgressDialog.show(context, null, "Please wait...", true, false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    runnable.run();
                }
            }, 5000);
        } else runnable.run();
    }

    void createPermission() {
        context.registerReceiver(usbConnectionReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        context.registerReceiver(usbConnectionReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        context.registerReceiver(usbConnectionReceiver, new IntentFilter(UsbConnectionReceiver.ACTION_USB_PERMISSION));
    }

    void createPermissionForBluetooth() {
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothPair.PAIRING_REQUEST));
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        context.registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
    }

    FingerPrintDevices getDeviceByProductIDAndVendorID(int pid, int vid) {
        return FingerPrintDevices.validateDevice(vid, pid);
    }

    @Override
    public void onImageReceived(Bitmap bitmap) {
        if (fingerPrintDeviceCallback != null) {
            fingerPrintDeviceCallback.onPreviewImage(bitmap);

//            String wsqImage = fingerPrintInitWrapper.getWsqImage();
//                            final SubscriberImageDetail subscriberImageDetail = new SubscriberImageDetail();
//                            subscriberImageDetail.setImageStr(wsqImage);

//            fingerPrintDeviceCallback.onImageDetailsCreated();
        }
    }

    @Override
    public void onPreviewImage(Bitmap bitmap) {
        if (fingerPrintDeviceCallback != null) {
            fingerPrintDeviceCallback.onPreviewImage(bitmap);
        }
    }

    @Override
    public void onError(final String error) {
        if(error!=null)
            fingerPrintDeviceCallback.onError(error);
    }

    @Override
    public void onFingerPrintDetailsCaptured(Object object) {
        fingerPrintDeviceCallback.onImageDetailsCreated(object);
    }

    void registerStartek() {
        final Intent piIntent = new Intent(UsbConnectionReceiver.ACTION_STARTEK_PERMISSION);
        mPermissionIntent = PendingIntent.getBroadcast(((Activity) context).getBaseContext(), 1, piIntent, 0); // requestCode must be 1, not 0
        context.registerReceiver(usbConnectionReceiver, new IntentFilter(UsbConnectionReceiver.ACTION_STARTEK_PERMISSION));
    }



    FingerPrintDevices checkIfValidDevicesConnected(ArrayList<UsbDevice> usbDevices) {
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(context, "CHECK if VALID DEVICES CONNECTED", Toast.LENGTH_SHORT).show();
//            }
//        });
        for (UsbDevice usbDevice : usbDevices) {
            FingerPrintDevices fingerPrintDevice =
                    getDeviceByProductIDAndVendorID(usbDevice.getProductId(), usbDevice.getVendorId());
            if (fingerPrintDevice != null) {
                askPermissionToInitialiseFingerPrintDevice(usbDevice);
                if (fingerPrintDevice == FingerPrintDevices.STARTEC) {
//                    StartekInitializer.usb_Dev = usbDevice;
                }
                currentFingerPrintDevice = fingerPrintDevice;
                return fingerPrintDevice;
            }
        }
        return null;
    }

    private void askPermissionToInitialiseFingerPrintDevice(final UsbDevice usbDevice) {
//        ((Activity) context).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (!manager.hasPermission(usbDevice)) {
            manager.requestPermission(usbDevice, mPermissionIntent);
        } else {
            fingerPrintDeviceCallback.onDeviceInitialized();
        }
//            }
//        });
    }

    public void autoInitializeFingerPrintDevice() {
        if (currentFingerPrintDevice != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    checkForDeviceInitialization();
                    initDeviceObject();
                }
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initializeFingerPrintDevice();
                        }
                    });
                }
            }, 2000);
        }
    }

    void callTimerForCallback() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(context, "AFTER TIMER CALLED", Toast.LENGTH_SHORT).show();
                        fingerPrintDeviceCallback.onDeviceInitialized();
                    }
                });
            }
        }, 8000);
    }

    @Override
    public void onInitialised() {
        fingerPrintDeviceCallback.onDeviceInitialized();
    }

    public void deRegisterDevices() {
        try {
            context.unregisterReceiver(usbConnectionReceiver);
            context.unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface FingerPrintDeviceCallback {
        void onPreviewImage(Bitmap bitmap);

        void onImageDetailsCreated(Object object);

        void onDeviceAttached(FingerPrintDevices device);

        void onDeviceDetached(FingerPrintDevices device);

        void onPermissionChanged(boolean status);

        void onDeviceInitialized();

        void onBluetoothFingerPrintDeviceSearchFailed();

        void onBluetoothSearchingStarted();

        void onBluetoothSearchingFinished();

        void onBluetoothPairingStarted();

        void onBluetoothPairingFinished();

        void onBluetoothPairingFailed();

        void onError(String errorText);

        BluetoothFingerPrintCallBack getBluetoothImp();
    }

    public interface BluetoothFingerPrintCallBack {
        void onBluetoothPairingFinished();

        void onBluetoothPairingFailed();

        void onDeviceDetached(FingerPrintDevices fingerPrintDevice);

        void onBluetoothSearchingStarted();

        void onBluetoothSearchingFinished();

        void onBluetoothFingerPrintDeviceSearchFailed();

        void onBluetoothPairingStarted();
    }
}

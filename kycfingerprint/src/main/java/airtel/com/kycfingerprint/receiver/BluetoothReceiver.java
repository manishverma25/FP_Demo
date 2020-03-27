package airtel.com.kycfingerprint.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import airtel.com.kycfingerprint.utility.ADLogger;;


/**
 * Created by A1P5KF3Z on 12/7/17.
 */

public class BluetoothReceiver extends BroadcastReceiver {

    public interface BluetoothReceiverCallback {
        void onBluetoothPairingChanged(BluetoothDevice device, boolean status);

        void onBluetoothDeviceFound(BluetoothDevice bluetoothDevice);

        void onBluetoothSearchingFinished();

        void onBluetoothSearchingStarted();
    }

    private BluetoothReceiverCallback bluetoothReceiverCallback;

    public BluetoothReceiver(BluetoothReceiverCallback bluetoothReceiverCallback) {
        this.bluetoothReceiverCallback = bluetoothReceiverCallback;
    }

    public BluetoothReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothDevice device = null;
        if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            Toast.makeText(context, "Bluetooth | Pairing Request |", Toast.LENGTH_SHORT).show();
            ADLogger.d("BLUETOOTH | Paring Request");
        } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
            bluetoothReceiverCallback.onBluetoothSearchingStarted();
            Toast.makeText(context, "Bluetooth | Discovery Started |", Toast.LENGTH_SHORT).show();
            ADLogger.d("BLUETOOTH | Discovery Started");
        } else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(context, "Bluetooth | BOND State | " + device.getBondState(), Toast.LENGTH_SHORT).show();
            ADLogger.d("BLUETOOTH | BOND State" + device.getBondState());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                bluetoothReceiverCallback.onBluetoothPairingChanged(device, true);
            else
                bluetoothReceiverCallback.onBluetoothPairingChanged(device, false);
        } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            bluetoothReceiverCallback.onBluetoothSearchingFinished();
            Toast.makeText(context, "Bluetooth | Discovery Finished", Toast.LENGTH_SHORT).show();
            ADLogger.d("BLUETOOTH | Discovery Finished");
        } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null && device.getName() != null) {
                bluetoothReceiverCallback.onBluetoothDeviceFound(device);
                ADLogger.d("BLUETOOTH | Device Found " + device.getName());
//                Toast.makeText(context, "Bluetooth | Bluetooth Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

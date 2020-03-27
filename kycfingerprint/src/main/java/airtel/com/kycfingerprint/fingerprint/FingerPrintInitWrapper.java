package airtel.com.kycfingerprint.fingerprint;

import android.content.Context;

import airtel.com.kycfingerprint.fingerprintDevices.FingerPrintDevices;

/**
 * Created by A1P5KF3Z on 2/23/17.
 */

public interface FingerPrintInitWrapper {

    boolean isInitialized();

    void initialize();

    void startCapture();

    FingerPrintDevices getDeviceType();

    void updateContext(Context context);
}

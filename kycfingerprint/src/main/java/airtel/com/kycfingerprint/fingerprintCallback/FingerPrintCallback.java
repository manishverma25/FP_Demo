package airtel.com.kycfingerprint.fingerprintCallback;

import android.graphics.Bitmap;

/**
 * Created by A1P5KF3Z on 1/12/17.
 */

public interface FingerPrintCallback {

    public void onImageReceived(Bitmap bitmap);

    public void onPreviewImage(Bitmap bitmap);

    public void onError(String error);

    public void onFingerPrintDetailsCaptured(Object object);

    void onInitialised();

}

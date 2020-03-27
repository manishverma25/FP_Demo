//package airtel.com.kycfingerprint.fingerprintDevices;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.AsyncTask;
//
//import com.mantra.FingerData;
//
//import airtel.com.kycfingerprint.fingerprint.FingerPrintInitWrapper;
//import airtel.com.kycfingerprint.fingerprintCallback.FingerPrintCallback;
//import airtel.com.kycfingerprint.fingerprintCallback.FingerPrintDataCallback;
//import airtel.com.kycfingerprint.utility.KycUtilityImage;
//
//
///**
// * Created by A1P5KF3Z on 2/23/17.
// */
//
//public class MantraInitializer implements MFS100Event, FingerPrintInitWrapper, FingerPrintDataCallback {
//
//    private Context context;
//    private MFS100 mfs100;
//    private FingerPrintCallback fingerPrintCallback;
//    private FingerData fingerData;
//    private boolean isInitialized = false;
//
//    public MantraInitializer(Context context, FingerPrintCallback fingerPrintCallback) {
//        this.context = context;
//        this.fingerPrintCallback = fingerPrintCallback;
//        try {
//            mfs100 = new MFS100(this, 41);
//            mfs100.SetApplicationContext(context);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void updateContext(Context context) {
//        this.context = context;
//        mfs100.SetApplicationContext(context);
//    }
//
//    @Override
//    public void initialize() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mfs100.Init();
//                isInitialized = true;
//            }
//        }).start();
//
//        // TODO: To be tested...
////        new InitializerTask().execute();
//    }
//
//    @Override
//    public boolean isInitialized() {
//        return isInitialized;
//    }
//
//    @Override
//    public FingerPrintDevices getDeviceType() {
//        return FingerPrintDevices.MANTRA;
//    }
//
//    @Override
//    public void startCapture() {
//        try {
//            int ret = mfs100.StartCapture(60, 4000, true);
//            if (ret != 0) {
//                fingerPrintCallback.onError(mfs100.GetErrorMsg(ret));
//            } else {
//                fingerPrintCallback.onError("Place finger on scanner");
//            }
//        } catch (Exception ex) {
//            fingerPrintCallback.onError("Error");
//        }
//    }
//
//    @Override
//    public void OnPreview(FingerData fingerData) {
//        this.fingerData = fingerData;
//        fingerPrintCallback.onImageReceived(KycUtilityImage.bytesToBitmap(fingerData.FingerImage()));
//    }
//
//    @Override
//    public void OnCaptureCompleted(boolean b, int i, String s, FingerData fingerData) {
//        if (fingerData != null) {
//            this.fingerData = fingerData;
//            fingerPrintCallback.onImageReceived(KycUtilityImage.bytesToBitmap(fingerData.FingerImage()));
//            fingerPrintCallback.onFingerPrintDetailsCaptured(fingerData.WSQImage());
//        }
//    }
//
//    @Override
//    public void OnDeviceAttached(int i, int i1, boolean b) {
//
//    }
//
//    @Override
//    public void OnDeviceDetached() {
//
//    }
//
//    @Override
//    public void OnHostCheckFailed(String s) {
//
//    }
//
//    @Override
//    public String getWsqImage() {
//        if (fingerData != null)
//            return KycUtilityImage.getBase64StringFromBytes(fingerData.WSQImage());
//        return "";
//    }
//
//    private class InitializerTask extends AsyncTask<Void, Void, Boolean> {
//
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(context, null, "Initializing...", true, false);
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            if (mfs100 != null)
//                mfs100.Init();
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            isInitialized = aBoolean;
//
//            dismissProgressDialog();
//        }
//
//        private void dismissProgressDialog() {
//            if (progressDialog != null && progressDialog.isShowing())
//                progressDialog.dismiss();
//        }
//
//        @Override
//        protected void onCancelled() {
//            dismissProgressDialog();
//        }
//    }
//}

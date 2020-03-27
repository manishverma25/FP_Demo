package airtel.com.kycfingerprint.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by A1P5KF3Z on 3/27/17.
 */

public class KycUtilityImage {

    public static Bitmap bytesToBitmap(byte[] byteArray) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(
                    byteArray, 0,
                    byteArray.length);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return bitmap;
        }
    }

    public static String getBase64StringFromBytes(byte[] data) {
        String str = "";
        try {
            str = Base64.encodeToString(data, 0);
            return str;
        } catch (Exception e) {

        } finally {
            return str;
        }
    }
}

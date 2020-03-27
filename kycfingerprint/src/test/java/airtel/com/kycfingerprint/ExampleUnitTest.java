package airtel.com.kycfingerprint;

import android.test.AndroidTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import airtel.com.kycfingerprint.fingerprintDevices.FingerPrintDevices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void validateDevice() {
        assertTrue(FingerPrintDevices.validateDevice(8797, 1));
        assertTrue(FingerPrintDevices.validateDevice(8797, 2));
        assertTrue(FingerPrintDevices.validateDevice(8797, 3));
        assertTrue(FingerPrintDevices.validateDevice(8797, 7));
        assertTrue(FingerPrintDevices.validateDevice(8797, 8));
        assertTrue(FingerPrintDevices.validateDevice(8797, 9));
        assertTrue(FingerPrintDevices.validateDevice(8797, 10));
        assertTrue(FingerPrintDevices.validateDevice(8797, 11));
        assertTrue(FingerPrintDevices.validateDevice(8797, 12));
        assertTrue(FingerPrintDevices.validateDevice(8797, 13));
        assertTrue(FingerPrintDevices.validateDevice(8797, 14));
        assertTrue(FingerPrintDevices.validateDevice(1947, 35));
        assertTrue(FingerPrintDevices.validateDevice(1947, 36));
        assertTrue(FingerPrintDevices.validateDevice(1947, 38));
        assertTrue(FingerPrintDevices.validateDevice(1947, 71));
        assertTrue(FingerPrintDevices.validateDevice(1947, 82));
        assertTrue(FingerPrintDevices.validateDevice(11279, 4101));
        assertTrue(FingerPrintDevices.validateDevice(11279, 4608));
        assertTrue(FingerPrintDevices.validateDevice(3018, 33317));

        assertFalse(FingerPrintDevices.validateDevice(30, 317));
        assertFalse(FingerPrintDevices.validateDevice(318, 3317));
        assertFalse(FingerPrintDevices.validateDevice(8, 337));
        assertFalse(FingerPrintDevices.validateDevice(308, 37));
    }
}
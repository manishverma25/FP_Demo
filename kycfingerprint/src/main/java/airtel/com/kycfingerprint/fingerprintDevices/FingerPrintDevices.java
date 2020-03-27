package airtel.com.kycfingerprint.fingerprintDevices;

import android.util.SparseArray;

import java.util.Objects;

/**
 * Created by A1P5KF3Z on 3/28/17.
 */
public enum FingerPrintDevices {

    MANTRA(
            new DeviceWrapper(11279, new Integer[]{4101})
    ),

    MANTRA_200(
            new DeviceWrapper(11279, new Integer[]{4608})
    ),

    STARTEC(
            new DeviceWrapper(3018, new Integer[]{33317})
    ),

    FUTRONIC(
            new DeviceWrapper(0, new Integer[]{0})
    ),

    EVOLUTE(
            new DeviceWrapper(0, new Integer[]{0})
    ),

    FAMOCO(
            new DeviceWrapper(1947, new Integer[]{35, 36, 38, 71, 82}),
            new DeviceWrapper(8797, new Integer[]{1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14})
    );

    public final SparseArray<Integer[]> deviceMap;

    FingerPrintDevices(DeviceWrapper... deviceWrappers) {
        deviceMap = new SparseArray<>();
        for (DeviceWrapper deviceWrapper : deviceWrappers) {
            deviceMap.put(deviceWrapper.vendorId, deviceWrapper.productIds);
        }
    }

    public static FingerPrintDevices validateDevice(Integer vendorId, Integer productId) {

        for (FingerPrintDevices fingerprintDevice : values()) {
            SparseArray<Integer[]> deviceMap = fingerprintDevice.deviceMap;
            Integer[] productIds = deviceMap.get(vendorId);
            if (productIds != null) for (Integer pId : productIds) {
                if (Objects.equals(pId, productId)) return fingerprintDevice;
            }
        }

        return null;

    }

    private static class DeviceWrapper {

        private final Integer vendorId;
        private final Integer[] productIds;

        private DeviceWrapper(Integer vendorId, Integer[] productIds) {
            this.vendorId = vendorId;
            this.productIds = productIds;
        }
    }
}

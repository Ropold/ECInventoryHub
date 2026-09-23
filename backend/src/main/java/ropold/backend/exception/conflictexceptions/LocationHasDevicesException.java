package ropold.backend.exception.conflictexceptions;

import java.util.List;

public class LocationHasDevicesException extends RuntimeException {
    private final List<String> deviceDetails;

    public LocationHasDevicesException(String message, List<String> deviceDetails) {
        super(message);
        this.deviceDetails = deviceDetails;
    }

    public List<String> getDeviceDetails() {
        return deviceDetails;
    }
}

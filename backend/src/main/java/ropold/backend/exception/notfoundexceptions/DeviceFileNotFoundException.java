package ropold.backend.exception.notfoundexceptions;

public class DeviceFileNotFoundException extends RuntimeException {
    public DeviceFileNotFoundException(String message) {
        super(message);
    }
}
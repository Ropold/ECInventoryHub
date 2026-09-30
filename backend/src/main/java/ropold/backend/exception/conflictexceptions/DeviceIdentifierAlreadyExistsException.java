package ropold.backend.exception.conflictexceptions;

import java.util.List;

public class DeviceIdentifierAlreadyExistsException extends RuntimeException {
    private final List<String> conflictDetails;

    public DeviceIdentifierAlreadyExistsException(String message, List<String> conflictDetails) {
        super(message);
        this.conflictDetails = conflictDetails;
    }

    public List<String> getConflictDetails() {
        return conflictDetails;
    }
}

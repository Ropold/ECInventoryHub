package ropold.backend.exception.conflictexceptions;

import java.util.List;

public class DeviceHasAssignmentsException extends RuntimeException {
    private final List<String> assignmentDetails;

    public DeviceHasAssignmentsException(String message, List<String> assignmentDetails) {
        super(message);
        this.assignmentDetails = assignmentDetails;
    }

    public List<String> getAssignmentDetails() {
        return assignmentDetails;
    }
}

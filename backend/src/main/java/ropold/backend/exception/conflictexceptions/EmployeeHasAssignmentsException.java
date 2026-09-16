package ropold.backend.exception.conflictexceptions;

import java.util.List;

public class EmployeeHasAssignmentsException extends RuntimeException {
    private final List<String> assignmentDetails;

    public EmployeeHasAssignmentsException(String message, List<String> assignmentDetails) {
        super(message);
        this.assignmentDetails = assignmentDetails;
    }

    public List<String> getAssignmentDetails() {
        return assignmentDetails;
    }
}

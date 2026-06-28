package ropold.backend.exception.notfoundexceptions;

public class AssignmentFileNotFoundException extends RuntimeException {
    public AssignmentFileNotFoundException(String message) {
        super(message);
    }
}
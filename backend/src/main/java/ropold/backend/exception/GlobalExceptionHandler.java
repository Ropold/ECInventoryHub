package ropold.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ropold.backend.exception.conflictexceptions.EmployeeHasAssignmentsException;
import ropold.backend.exception.conflictexceptions.LocationHasDevicesException;
import ropold.backend.exception.notfoundexceptions.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return new ErrorResponse("ACCESS_DENIED", e.getMessage());
    }

    @ExceptionHandler(EmployeeHasAssignmentsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmployeeHasAssignmentsException(EmployeeHasAssignmentsException e) {
        log.warn("Conflict: {}", e.getMessage());
        return new ErrorResponse("EMPLOYEE_HAS_ASSIGNMENTS", e.getMessage(), e.getAssignmentDetails());
    }

    @ExceptionHandler(LocationHasDevicesException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleLocationHasDevicesException(LocationHasDevicesException e) {
        log.warn("Conflict: {}", e.getMessage());
        return new ErrorResponse("LOCATION_HAS_DEVICES", e.getMessage(), e.getDeviceDetails());
    }

    @ExceptionHandler({
            AssignmentNotFoundException.class,
            AssignmentFileNotFoundException.class,
            DeviceNotFoundException.class,
            DeviceFileNotFoundException.class,
            EmployeeNotFoundException.class,
            LocationNotFoundException.class,
            UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(RuntimeException e) {
        log.error("NotFoundException: {}", e.getMessage(), e);
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException e) {
        log.error("Unhandled RuntimeException: {}", e.getMessage(), e);
        return new ErrorResponse("INTERNAL_ERROR", e.getMessage());
    }

}

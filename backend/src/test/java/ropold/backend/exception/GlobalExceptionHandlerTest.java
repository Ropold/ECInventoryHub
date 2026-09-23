package ropold.backend.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ropold.backend.exception.conflictexceptions.EmployeeHasAssignmentsException;
import ropold.backend.exception.conflictexceptions.LocationHasDevicesException;
import ropold.backend.exception.notfoundexceptions.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Something went wrong");

        ErrorResponse response = globalExceptionHandler.handleRuntimeException(exception);

        assertThat(response.code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.message()).isEqualTo("Something went wrong");
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ErrorResponse response = globalExceptionHandler.handleAccessDeniedException(exception);

        assertThat(response.code()).isEqualTo("ACCESS_DENIED");
        assertThat(response.message()).isEqualTo("Access denied");
    }

    @Test
    void testHandleEmployeeHasAssignmentsException() {
        EmployeeHasAssignmentsException exception = new EmployeeHasAssignmentsException(
                "Employee cannot be deleted because there are still assignments referencing them.",
                List.of("Assignment ID: 123"));

        ErrorResponse response = globalExceptionHandler.handleEmployeeHasAssignmentsException(exception);

        assertThat(response.code()).isEqualTo("EMPLOYEE_HAS_ASSIGNMENTS");
        assertThat(response.message()).isEqualTo("Employee cannot be deleted because there are still assignments referencing them.");
        assertThat(response.details()).containsExactly("Assignment ID: 123");
    }

    @Test
    void testHandleLocationHasDevicesException() {
        LocationHasDevicesException exception = new LocationHasDevicesException(
                "Location cannot be deleted because there are still devices referencing it.",
                List.of("Device ID: 123"));

        ErrorResponse response = globalExceptionHandler.handleLocationHasDevicesException(exception);

        assertThat(response.code()).isEqualTo("LOCATION_HAS_DEVICES");
        assertThat(response.message()).isEqualTo("Location cannot be deleted because there are still devices referencing it.");
        assertThat(response.details()).containsExactly("Device ID: 123");
    }

    @Test
    void testHandleNotFoundException_AssignmentNotFound() {
        AssignmentNotFoundException exception = new AssignmentNotFoundException("Assignment not found with id: 123");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Assignment not found with id: 123");
    }

    @Test
    void testHandleNotFoundException_AssignmentFileNotFound() {
        AssignmentFileNotFoundException exception = new AssignmentFileNotFoundException("Assignment file not found");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Assignment file not found");
    }

    @Test
    void testHandleNotFoundException_DeviceNotFound() {
        DeviceNotFoundException exception = new DeviceNotFoundException("Device not found with id: 123");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Device not found with id: 123");
    }

    @Test
    void testHandleNotFoundException_DeviceFileNotFound() {
        DeviceFileNotFoundException exception = new DeviceFileNotFoundException("Device file not found");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Device file not found");
    }

    @Test
    void testHandleNotFoundException_EmployeeNotFound() {
        EmployeeNotFoundException exception = new EmployeeNotFoundException("Employee not found with id: 123");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Employee not found with id: 123");
    }

    @Test
    void testHandleNotFoundException_LocationNotFound() {
        LocationNotFoundException exception = new LocationNotFoundException("Location not found with id: 123");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("Location not found with id: 123");
    }

    @Test
    void testHandleNotFoundException_UserNotFound() {
        UserNotFoundException exception = new UserNotFoundException("User not found");

        ErrorResponse response = globalExceptionHandler.handleNotFoundException(exception);

        assertThat(response.code()).isEqualTo("NOT_FOUND");
        assertThat(response.message()).isEqualTo("User not found");
    }
}
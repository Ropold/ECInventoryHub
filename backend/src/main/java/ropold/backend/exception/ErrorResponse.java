package ropold.backend.exception;

public record ErrorResponse(
        String code,
        String message
) {
}

package com.broadcns.msauthserver.exception;

import com.broadcns.msauthserver.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    //    @ExceptionHandler(JwtTokenExpiredException.class)
//    public ResponseEntity<ErrorResponse> handleJwtTokenExpiredException(
//            JwtTokenExpiredException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                HttpStatus.UNAUTHORIZED.value(),
//                ex.getMessage(),
//                null
//        );
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
//
//    @ExceptionHandler(GitlabAuthenticationException.class)
//    public ResponseEntity<ErrorResponse> handleGitlabAuthenticationException(
//            GitlabAuthenticationException ex, HttpServletRequest request) {
//        log.error("GitLab authentication failed: {}", ex.getMessage());
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.UNAUTHORIZED.value(),
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//    }
//
//    @ExceptionHandler(InvalidTokenException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
//            InvalidTokenException ex, HttpServletRequest request) {
//        log.error("Invalid token: {}", ex.getMessage());
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.UNAUTHORIZED.value(),
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
//            AccessDeniedException ex, HttpServletRequest request) {
//        log.error("Access denied: {}", ex.getMessage());
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.FORBIDDEN.value(),
//                "접근 권한이 없습니다.",
//                request.getRequestURI()
//        );
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }
//
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

    }
}

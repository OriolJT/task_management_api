package com.orioljt.taskmanager.exception;

import com.orioljt.taskmanager.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Centralized REST error handling producing a consistent problem-style JSON body. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    Map<String, List<String>> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));
    return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, List<String>> fieldErrors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.groupingBy(
                    v -> v.getPropertyPath().toString(),
                    Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())));
    return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
    ex.getMostSpecificCause();
    String message = ex.getMostSpecificCause().getMessage();
    Map<String, List<String>> fieldErrors = null;
    if (message != null && message.toLowerCase().contains("email")) {
      fieldErrors = Map.of("email", List.of("email already exists"));
    }
    return build(HttpStatus.CONFLICT, "Data integrity violation", fieldErrors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGeneric(Exception ex, WebRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", null);
  }

  private ResponseEntity<Object> build(
      HttpStatus status, String message, Map<String, List<String>> fieldErrors) {
    ErrorResponse body =
        ErrorResponse.of(status.value(), status.getReasonPhrase(), message, fieldErrors);
    return ResponseEntity.status(status).body(body);
  }
}

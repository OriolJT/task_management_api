package com.orioljt.taskmanager.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.orioljt.taskmanager.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

  GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @SuppressWarnings("unused")
  private void dummy(String arg) {}

  @Test
  void handleNotFound_returns404() {
  ResponseEntity<Object> resp = handler.handleNotFound(new NotFoundException("Missing"), mock(WebRequest.class));
  assertThat(resp.getStatusCode().value()).isEqualTo(404);
  ErrorResponse body = (ErrorResponse) java.util.Objects.requireNonNull(resp.getBody());
  assertThat(body.message()).contains("Missing");
  }

  @Test
  void handleMethodArgumentNotValid_groupsFieldErrors() throws Exception {
  record Dto(String name) {}
  BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Dto(""), "dto");
    br.addError(new FieldError("dto", "name", "must not be blank"));
  MethodParameter param = new MethodParameter(this.getClass().getDeclaredMethod("dummy", String.class), 0);
  MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, br);
  ResponseEntity<Object> resp = handler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));
      Assertions.assertNotNull(resp);
      assertThat(resp.getStatusCode().value()).isEqualTo(400);
  ErrorResponse body = (ErrorResponse) java.util.Objects.requireNonNull(resp.getBody());
  assertThat(body.fieldErrors().get("name")).contains("must not be blank");
  }

  @Test
  void handleConstraintViolation_groupsMessages() {
  @SuppressWarnings("unchecked")
  ConstraintViolation<Object> v = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
    jakarta.validation.Path path = mock(jakarta.validation.Path.class);
    org.mockito.Mockito.when(path.toString()).thenReturn("list.page");
    org.mockito.Mockito.when(v.getPropertyPath()).thenReturn(path);
    org.mockito.Mockito.when(v.getMessage()).thenReturn("must be >= 0");
  java.util.Set<ConstraintViolation<?>> set = java.util.Set.of(v);
  ConstraintViolationException ex = new ConstraintViolationException("bad", set);
  ResponseEntity<Object> resp = handler.handleConstraintViolation(ex);
  ErrorResponse body = (ErrorResponse) java.util.Objects.requireNonNull(resp.getBody());
    assertThat(body.fieldErrors().get("list.page")).contains("must be >= 0");
  }

  @Test
  void handleDataIntegrity_mapsEmailConflict() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("dup", new RuntimeException("email unique violation"));
  ResponseEntity<Object> resp = handler.handleDataIntegrity(ex);
  ErrorResponse body = (ErrorResponse) java.util.Objects.requireNonNull(resp.getBody());
  assertThat(body.status()).isEqualTo(409);
  assertThat(body.fieldErrors()).isEqualTo(Map.of("email", List.of("email already exists")));
  }

  @Test
  void handleGeneric_returns500() {
  ResponseEntity<Object> resp = handler.handleGeneric(new RuntimeException("boom"), mock(WebRequest.class));
  ErrorResponse body = (ErrorResponse) java.util.Objects.requireNonNull(resp.getBody());
  assertThat(body.status()).isEqualTo(500);
  }
}

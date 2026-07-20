package edu.tamu.catalog.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * The index end point exception to REST response converter.
 *
 * This is also used as a global exception handler.
 */
@ControllerAdvice
public class IndexAdvice extends AbstractAdvice {

  /**
   * The object mapper for this class.
   */
  final ObjectMapper objectMapper;

  /**
   * Initializer.
   *
   * @param objectMapper The object mapper.
   */
  IndexAdvice(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {

    return buildError(exception, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON, "Invalid request received.");
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {

    return buildError(exception, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<String> handleUnsupportedOperationException(UnsupportedOperationException exception) {

    return buildError(exception, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<String> handleNoHandlerFoundException(NoHandlerFoundException exception) {

    return buildError(exception, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public ResponseEntity<String> handleHttpClientErrorException(HttpClientErrorException exception) {

    return buildError(exception, exception.getStatusCode());
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity<String> handleHttpServerErrorException(HttpServerErrorException exception) {

    return buildError(exception, exception.getStatusCode());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {

    return buildError(exception, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  protected ObjectMapper getObjectMapper() {

    return objectMapper;
  }

}

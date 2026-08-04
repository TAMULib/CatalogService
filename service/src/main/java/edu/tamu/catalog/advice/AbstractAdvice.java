package edu.tamu.catalog.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tamu.catalog.enums.ResponseItemEnum;
import edu.tamu.catalog.response.JsonResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Provide basic error handling and reporting, defaulting as JSON responses.
 */
public abstract class AbstractAdvice extends RequestMappingHandlerMapping {

  /**
   * Get the object mapper.
   *
   * @return objectMapper The object mapper.
   */
  protected abstract ObjectMapper getObjectMapper();

  /**
   * Build the error message, with default JSON media type.
   *
   * @param ex   The exception.
   * @param code The HTTP Status Code.
   *
   * @return The built error response entity.
   */
  protected ResponseEntity<String> buildError(Exception ex, HttpStatus code) {

    return buildError(ex, code, MediaType.APPLICATION_JSON);
  }

  /**
   * Build the error message without using custom message.
   *
   * @param ex   The exception.
   * @param code The HTTP Status Code.
   * @param type The media type to use.
   *
   * @return The built error response entity.
   */
  protected ResponseEntity<String> buildError(Exception ex, HttpStatus code, MediaType type) {

    return buildError(ex, code, type, null);
  }

  /**
   * Build the error message.
   *
   * Only the JSON media type is converted, all others are passed through as-is.
   *
   * This will log non-empty details to the console log if the exception is an instance of AbstractDetailException.
   *
   * @param ex      The exception.
   * @param code    The HTTP Status Code.
   * @param type    The media type to use.
   * @param message A custom error message to use rather than from a stack trace. If NULL, then the stack trace message is used.
   *
   * @return The built error response entity.
   *
   * @throws JsonProcessingException on error.
   */
  protected ResponseEntity<String> buildError(Exception ex, HttpStatus code, MediaType type, String message) {

    final String item = message == null && ex != null ? ex.getMessage() : message;
    String response = item;

    logger.error(response, logger.isDebugEnabled() ? ex : null);

    if (type == MediaType.APPLICATION_JSON) {
      final List<String> items = item == null ? new ArrayList<>() : List.of(item);
      final JsonResponse<String> json = new JsonResponse<>();

      json.setStatus(code);
      json.setType(ResponseItemEnum.STRING.toString().toLowerCase());
      json.setTotal(items.size());
      json.setItems(items);

      try {
        response = getObjectMapper().writeValueAsString(json);
      } catch (JsonProcessingException e) {
        if (items.isEmpty()) {
          response = items.get(0);
        } else {
          response = ex == null ? "" : ex.getMessage();
        }

        logger.error("Failed to perform JSON deserialization when constructing the error response packet, falling back to raw string", e);
      }
    }

    return ResponseEntity.status(code)
      .contentType(type)
      .body(response);
  }

}

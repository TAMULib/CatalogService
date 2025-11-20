package edu.tamu.catalog.exception;

import static edu.tamu.weaver.response.ApiStatus.ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tamu.weaver.response.ApiResponse;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String RENEWAL_DID_NOT_CHANGE_THE_DUE_DATE = "Renewal did not change the due date";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BibIdNotFoundError.class)
    public ResponseEntity<String> bibIdNotFoundError(BibIdNotFoundError e, WebRequest request) {
        logErrors(e);

        return buildApiResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);
    }

    @ExceptionHandler(RenewFailureException.class)
    public ResponseEntity<String> renewError(RenewFailureException e, WebRequest request) {
        logErrors(e);

        return buildApiResponseEntity(RENEWAL_DID_NOT_CHANGE_THE_DUE_DATE, HttpStatus.UNPROCESSABLE_ENTITY, MediaType.APPLICATION_JSON);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> clientError(HttpClientErrorException e, WebRequest request) {
        logErrors(e);

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> serverError(HttpServerErrorException e, WebRequest request) {
        logErrors(e);

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> unsupportedOperationError(UnsupportedOperationException e, WebRequest request) {
        logErrors(e);

        return buildApiResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> parseError(IllegalArgumentException e, WebRequest request) {
        logErrors(e);

        return buildApiResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> parseError(DateTimeParseException e, WebRequest request) {
        logErrors(e);

        return buildApiResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON);
    }

    /**
     * Handle the logging of the exceptions.
     *
     * @param e The exception to log.
     */
    private void logErrors(Exception e) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }
    }

    /**
     * Build response message as an API Response.
     *
     * This allows for passing through the HTTP status codes while still preserving the API Response behavior.
     *
     * @param message The message in the API Response.
     * @param status The status code.
     * @param type The HTTP payload type.
     *
     * @return The constructed resonse entity.
     */
    private ResponseEntity<String> buildApiResponseEntity(String message, HttpStatus status, MediaType type) {
        ObjectMapper mapper = new ObjectMapper();

        // The exception handler should ideally not throw its own exceptions.
        // Catch the exceptions and report it, then fall back to a plain text error message.
        try {
            message = mapper.writeValueAsString(new ApiResponse(ERROR, message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            type = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.status(status)
            .contentType(type)
            .body(message);
    }

}

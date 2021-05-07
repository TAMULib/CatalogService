package edu.tamu.catalog.exception;

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


    @ExceptionHandler(RenewFailureException.class)
    public ResponseEntity<String> renewError(RenewFailureException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .contentType(MediaType.TEXT_PLAIN)
            .body(RENEWAL_DID_NOT_CHANGE_THE_DUE_DATE);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> clientError(HttpClientErrorException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            logger.warn(e.getResponseBodyAsString());
            e.printStackTrace();
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> serverError(HttpServerErrorException e, WebRequest request) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
            logger.error(e.getResponseBodyAsString());
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> unsupportedOperationError(UnsupportedOperationException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.TEXT_PLAIN)
            .body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> parseError(IllegalArgumentException e, WebRequest request) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.TEXT_PLAIN)
            .body(e.getMessage());
    }

}

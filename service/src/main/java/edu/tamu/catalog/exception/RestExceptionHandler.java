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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> clientError(HttpClientErrorException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(e.getMessage());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> serverError(HttpServerErrorException e, WebRequest request) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(e.getMessage());
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

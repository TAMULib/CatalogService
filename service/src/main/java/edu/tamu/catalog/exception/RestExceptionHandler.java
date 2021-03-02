package edu.tamu.catalog.exception;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler({RestClientResponseException.class})
    public ResponseEntity<String> clientError(HttpClientErrorException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .body(e.getMessage());
    }

    @ExceptionHandler({HttpServerErrorException.class})
    public ResponseEntity<String> serverError(HttpServerErrorException e, WebRequest request) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(e.getRawStatusCode())
            .body(e.getMessage());
    }

    @ExceptionHandler({UnsupportedOperationException.class})
    public ResponseEntity<String> unsupportedOperationError(UnsupportedOperationException e, WebRequest request) {
        logger.warn(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(e.getMessage());
    }

    @ExceptionHandler({ParseException.class})
    public ResponseEntity<String> parseError(ParseException e, WebRequest request) {
        logger.error(e.getMessage());

        if (logger.isDebugEnabled()) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }

}

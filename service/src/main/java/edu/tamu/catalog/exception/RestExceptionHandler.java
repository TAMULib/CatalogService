package edu.tamu.catalog.exception;

import java.text.ParseException;

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

    @ExceptionHandler({RestClientResponseException.class})
    public ResponseEntity<String> clientError(HttpClientErrorException e, WebRequest request) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(e.getRawStatusCode()));
    }

    @ExceptionHandler({HttpServerErrorException.class})
    public ResponseEntity<String> serverError(HttpServerErrorException e, WebRequest request) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(e.getRawStatusCode()));
    }

    @ExceptionHandler({UnsupportedOperationException.class})
    public ResponseEntity<String> unsupportedOperationError(UnsupportedOperationException e, WebRequest request) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ParseException.class})
    public ResponseEntity<String> parseError(ParseException e, WebRequest request) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

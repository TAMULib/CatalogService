package edu.tamu.catalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({RestClientResponseException.class})
    public ResponseEntity<String> notFound(RestClientResponseException e, WebRequest request) {

        if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        throw e;
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> notFound(NotFoundException e, WebRequest request) {
        return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
    }
}

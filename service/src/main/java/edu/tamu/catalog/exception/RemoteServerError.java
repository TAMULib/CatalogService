package edu.tamu.catalog.exception;

import org.springframework.http.HttpStatus;

public class RemoteServerError extends Exception {

    private static final long serialVersionUID = -22660189803028941L;

    private static final String MESSAGE = "Failed to access remote server for %s catalog due to %s.";

    private String details;

    private String method;

    private HttpStatus statusCode;

    private String url;

    public RemoteServerError(String method, String url, String catalog, HttpStatus statusCode, String message) {
        super(String.format(MESSAGE, catalog, statusCode.toString()));

        this.details = message;
        this.method = method;
        this.statusCode = statusCode;
        this.url = url;
    }

    /**
     * The response message associated with this exception.
     *
     *  @return The response message.
     */
    public String getDetails() {
        return details;
    }

    /**
     * The request method associated with this exception.
     *
     *  @return The request method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * The response status code associated with this exception.
     *
     *  @return The request method.
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * The URL associated with this exception.
     *
     *  @return The URL.
     */
    public String getUrl() {
        return url;
    }

}

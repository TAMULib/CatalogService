package edu.tamu.catalog.exception;

import java.nio.charset.Charset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpServerErrorException;

public class CatalogHttpServerException extends HttpServerErrorException {

  private static final long serialVersionUID = 346211115584311289L;

  /**
   * Constructor with a status code only.
   */
  public CatalogHttpServerException(HttpStatus statusCode) {
    super(statusCode);
  }

  /**
   * Constructor with a status code and status text.
   */
  public CatalogHttpServerException(HttpStatus statusCode, String statusText) {
    super(statusCode, statusText);
  }

  /**
   * Constructor with a status code and status text, and content.
   */
  public CatalogHttpServerException(
      HttpStatus statusCode, String statusText, @Nullable byte[] body, @Nullable Charset responseCharset) {

    super(statusCode, statusText, body, responseCharset);
  }

  /**
   * Constructor with a status code and status text, headers, and content.
   */
  public CatalogHttpServerException(HttpStatus statusCode, String statusText,
      @Nullable HttpHeaders headers, @Nullable byte[] body, @Nullable Charset responseCharset) {

    super(statusCode, statusText, headers, body, responseCharset);
  }

  /**
   * Constructor with a status code and status text, headers, and content,
   * and a prepared message.
   * @since 5.2.2
   */
  public CatalogHttpServerException(String message, HttpStatus statusCode, String statusText,
      @Nullable HttpHeaders headers, @Nullable byte[] body, @Nullable Charset responseCharset) {

    super(message, statusCode, statusText, headers, body, responseCharset);
  }

}

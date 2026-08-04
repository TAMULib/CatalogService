package edu.tamu.catalog.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Provide a basic top-level structure for all JSON response types.
 *
 * This is designed around the JDK14+ `record` type.
 *
 * @param <T>    The item list type (this can be something as simple as a string).
 * @param status The HTTP Response status (added to the object in addition to the HTTP header).
 * @param type   The type of this message (as a string to act as a generic or abstract structure).
 * @param total  The total number of rows in the items array.
 * @param items  An array of all items attached (this can be any structure and can even be recursive to some extent).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonResponse<T> implements Serializable {

  private static final long serialVersionUID = 32112693629109L;

  @JsonProperty("status")
  private HttpStatus status;

  @JsonProperty("type")
  private String type;

  @JsonProperty("total")
  private Integer total;

  @JsonProperty("items")
  private List<T> items;

  /**
   * @return the status
   */
  public HttpStatus getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the total
   */
  public Integer getTotal() {
    return total;
  }

  /**
   * @param total the total to set
   */
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   * @return the items
   */
  public List<T> getItems() {
    return items;
  }

  /**
   * @param items the items to set
   */
  public void setItems(List<T> items) {
    this.items = items;
  }

}

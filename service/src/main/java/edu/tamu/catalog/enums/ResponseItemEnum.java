package edu.tamu.catalog.enums;

/**
 * Response types for JsonResponse items.
 */
public enum ResponseItemEnum {

  /**
   * The response type is in HTML.
   */
  HTML,

  /**
   * The response type is in JSON with no explicitly defined structure.
   *
   * This could included embedded JsonResponse.
   */
  JSON,

  /**
   * The response type is text of no described structure.
   */
  STRING,

}

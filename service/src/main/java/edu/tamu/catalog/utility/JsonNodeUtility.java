package edu.tamu.catalog.utility;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeUtility {

    private JsonNodeUtility() {

    }

    /**
     * Get string value from JsonNode. Return null if value not found.
     *
     * @param input       JsonNode
     * @param jsonPtrExpr String
     * @return string value
     */
    public static String getText(JsonNode input, String jsonPtrExpr) {
        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? property.asText() : null;
    }

    /**
     * Get integer value from JsonNode. Return null if value not found.
     *
     * @param input       JsonNode
     * @param jsonPtrExpr String
     * @return integer value
     */
    public static Integer getInt(JsonNode input, String jsonPtrExpr) {
        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? property.asInt() : null;
    }

    /**
     * Get double value from JsonNode. Return default value if not found.
     *
     * @param input        JsonNode
     * @param jsonPtrExpr  String
     * @param defaultValue double
     * @return double value
     */
    public static Double getDouble(JsonNode input, String jsonPtrExpr, double defaultValue) {
        Double value = getDouble(input, jsonPtrExpr);
        return Objects.nonNull(value) ? value : defaultValue;
    }

    /**
     * Get double value from JsonNode. Return null if value not found.
     *
     * @param input       JsonNode
     * @param jsonPtrExpr String
     * @return integer value
     */
    public static Double getDouble(JsonNode input, String jsonPtrExpr) {
        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? property.asDouble() : null;
    }

    /**
     * Get boolean value from JsonNode. Return default value if not found.
     * 
     * @param input       JsonNode
     * @param jsonPtrExpr String
     * @return boolean value
     */
    public static Boolean getBoolean(JsonNode input, String jsonPtrExpr, boolean defaultValue) {
        Boolean value = getBoolean(input, jsonPtrExpr);
        return Objects.nonNull(value) ? value : defaultValue;
    }

    /**
     * Get boolean value from JsonNode. Return null if value not found.
     * 
     * @param input       JsonNode
     * @param jsonPtrExpr String
     * @return boolean value
     */
    public static Boolean getBoolean(JsonNode input, String jsonPtrExpr) {
        JsonNode property = input.at(jsonPtrExpr);
        return property.isValueNode() ? property.booleanValue() : null;
    }

}

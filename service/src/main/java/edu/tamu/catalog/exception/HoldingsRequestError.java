package edu.tamu.catalog.exception;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HoldingsRequestError extends Exception {

    private static final long serialVersionUID = 1672631211195165509L;

    private static final String ERROR_ATTR_CODE = "code";
    private static final String MESSAGE = "Failed to get holdings for %s catalog due to: %s.";

    public HoldingsRequestError(NodeList errorNodes, String catalog) {
        super(buildMessage(errorNodes, catalog));
    }

    /**
     * Construct the message string.
     *
     * @param errorNodes The error nodes returned by a restTemplate.getForObject() call.
     * @param catalog The catalog associated with the error.
     *
     * @return The constructed string.
     */
    private static String buildMessage(NodeList errorNodes, String catalog) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < errorNodes.getLength(); i++) {
            Node node = errorNodes.item(i);
            Node code = node.getAttributes().getNamedItem(ERROR_ATTR_CODE);

            String codeValue = code == null ? "" : code.getTextContent();
            String nodeValue = node == null ? "" : node.getTextContent();

            builder.append(String.format("Error '%s': %s ", codeValue, nodeValue));
        }

        // Remove the trailing space.
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        // Remove the trailing period.
        if (builder.length() > 0) {
            if (builder.charAt(builder.length() - 1) == '.') {
                builder.setLength(builder.length() - 1);
            }
        }

        return String.format(MESSAGE, catalog, builder.toString());
    }

}

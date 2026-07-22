package edu.tamu.catalog.exception;

public class BibIdNotFoundError extends Exception {

    private static final long serialVersionUID = 1005770155872425525L;

    private static final String MESSAGE = "No instances found with a Bibliographic ID of %s for %s catalog.";

    public BibIdNotFoundError(String id, String catalog) {
        super(String.format(MESSAGE, id, catalog));
    }

}

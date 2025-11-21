package edu.tamu.catalog.exception;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class RemoteServerErrorTest {

    private static final String CATALOG = "Some Catalog";
    private static final String DETAILS = "The details.";
    private static final String GET = "HTTP GET";
    private static final String URL = "http://localhost/some/url";

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    @Test
    void remoteServerErrorWorksTest() {
        RemoteServerError exception = Assertions.assertThrows(RemoteServerError.class, () -> {
            throw new RemoteServerError(GET, URL, CATALOG, STATUS, DETAILS);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(CATALOG));
        assertTrue(exception.getMessage().contains(STATUS.toString()));

        // These are printed to the system log rather than the user and should not appear in the message itself.
        assertFalse(exception.getMessage().contains(DETAILS));
        assertFalse(exception.getMessage().contains(GET));
        assertFalse(exception.getMessage().contains(URL));
    }

}

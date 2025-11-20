package edu.tamu.catalog.exception;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BibIdNotFoundErrorTest {

    private static final String ID = "The ID.";
    private static final String CATALOG = "Some Catalog";

    @Test
    void delegateSpinFailureWorksTest() throws IOException {
          BibIdNotFoundError exception = Assertions.assertThrows(BibIdNotFoundError.class, () -> {
          throw new BibIdNotFoundError(ID, CATALOG);
      });

      assertNotNull(exception);
      assertTrue(exception.getMessage().contains(ID));
      assertTrue(exception.getMessage().contains(CATALOG));
    }

}

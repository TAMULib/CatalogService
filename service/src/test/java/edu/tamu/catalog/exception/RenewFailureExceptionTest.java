package edu.tamu.catalog.exception;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RenewFailureExceptionTest {

    private static final String MESSAGE = "Some Message";

    @Test
    void delegateSpinFailureWorksTest() throws IOException {
          RenewFailureException exception = Assertions.assertThrows(RenewFailureException.class, () -> {
          throw new RenewFailureException(MESSAGE);
      });

      assertNotNull(exception);
      assertTrue(exception.getMessage().contains(MESSAGE));
   }

}

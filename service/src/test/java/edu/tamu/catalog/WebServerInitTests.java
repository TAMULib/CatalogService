package edu.tamu.catalog;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(classes = { WebServerInit.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public final class WebServerInitTests {

    @Test
    public void contextLoads() {
        assertTrue(true);
    }

}

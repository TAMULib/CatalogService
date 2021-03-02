package edu.tamu.catalog;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { WebServerInit.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
public final class WebServerInitTests {

    @Test
    public void contextLoads() {
        assertTrue(true);
    }

}


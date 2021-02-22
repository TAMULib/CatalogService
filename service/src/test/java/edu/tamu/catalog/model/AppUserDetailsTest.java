package edu.tamu.catalog.model;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import edu.tamu.catalog.auth.model.AppUserDetails;
import edu.tamu.catalog.enums.Role;
import edu.tamu.weaver.auth.model.Credentials;

@RunWith(SpringRunner.class)
public class AppUserDetailsTest {

    private static final Credentials TEST_CREDENTIALS = new Credentials();
    static {
        TEST_CREDENTIALS.setUin("123456789");
        TEST_CREDENTIALS.setEmail("aggieJack@tamu.edu");
        TEST_CREDENTIALS.setFirstName("Aggie");
        TEST_CREDENTIALS.setLastName("Jack");
        TEST_CREDENTIALS.setRole("ROLE_USER");
    }

    @Test
    public void testCreateUserModelViaUserModel() {
        AppUserDetails testUser = new AppUserDetails(createUserModel());

        assertTrue("Test user name matches", testUser.getUsername().equals(TEST_CREDENTIALS.getUin()));
        // TODO: requires fixes in the User model for these test.
        //assertTrue("Test user email matches", testUser.getEmail().equals(TEST_CREDENTIALS.getEmail()));
        //assertTrue("Test user netid matches", testUser.getNetid().equals(TEST_CREDENTIALS.getNetid()));
        assertTrue("Test user first name matches", testUser.getFirstName().equals(TEST_CREDENTIALS.getFirstName()));
        assertTrue("Test user last name matches", testUser.getLastName().equals(TEST_CREDENTIALS.getLastName()));
        assertTrue("Test user role matches", testUser.getRole().toString().equals(TEST_CREDENTIALS.getRole()));
        assertTrue("Test user password is empty", StringUtils.isEmpty(testUser.getPassword()));
    }
    
    private User createUserModel() {
      User testUser = new User();

      testUser.setUsername(TEST_CREDENTIALS.getUin());
      testUser.setEmail(TEST_CREDENTIALS.getEmail());
      testUser.setFirstName(TEST_CREDENTIALS.getFirstName());
      testUser.setLastName(TEST_CREDENTIALS.getLastName());
      testUser.setRole(Role.valueOf(TEST_CREDENTIALS.getRole()));

      return testUser;
    }

}
package edu.tamu.catalog.model;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.catalog.enums.Role;
import edu.tamu.weaver.auth.model.Credentials;

@RunWith(SpringRunner.class)
public class UserTest {

    @Value("classpath:mock/credentials/aggiejack.json")
    private Resource aggieJackCredentialsResource;

    private Credentials aggieJackCredentials;

    @Spy
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        aggieJackCredentials = getMockAggieJackCredentials();
    }

    @Test
    public void testCreateUserModelViaSetMethod() {
        User testUser = new User();

        testUser.setUsername(aggieJackCredentials.getUin());
        testUser.setEmail(aggieJackCredentials.getEmail());
        testUser.setFirstName(aggieJackCredentials.getFirstName());
        testUser.setLastName(aggieJackCredentials.getLastName());
        testUser.setNetid(aggieJackCredentials.getNetid());
        testUser.setRole(Role.valueOf(aggieJackCredentials.getRole()));

        assertTrue("Test user name matches", testUser.getUsername().equals(aggieJackCredentials.getUin()));
        // TODO: requires fixes in the User model for these test.
        //assertTrue("Test user email matches", testUser.getEmail().equals(aggieJackCredentials.getEmail()));
        //assertTrue("Test user netid matches", testUser.getNetid().equals(aggieJackCredentials.getNetid()));
        assertTrue("Test user first name matches", testUser.getFirstName().equals(aggieJackCredentials.getFirstName()));
        assertTrue("Test user last name matches", testUser.getLastName().equals(aggieJackCredentials.getLastName()));
        assertTrue("Test user role matches", testUser.getRole().toString().equals(aggieJackCredentials.getRole()));
        assertTrue("Test user password is empty", StringUtils.isEmpty(testUser.getPassword()));
    }

    @Test
    public void testCreateUserModelViaUserModel() {
        User testUser = new User(aggieJackCredentials);

        assertTrue("Test user name matches", testUser.getUsername().equals(aggieJackCredentials.getUin()));
        // TODO: requires fixes in the User model for these test.
        //assertTrue("Test user email matches", testUser.getEmail().equals(aggieJackCredentials.getEmail()));
        //assertTrue("Test user netid matches", testUser.getNetid().equals(aggieJackCredentials.getNetid()));
        assertTrue("Test user first name matches", testUser.getFirstName().equals(aggieJackCredentials.getFirstName()));
        assertTrue("Test user last name matches", testUser.getLastName().equals(aggieJackCredentials.getLastName()));
        assertTrue("Test user role matches", testUser.getRole().toString().equals(aggieJackCredentials.getRole()));
        assertTrue("Test user password is empty", StringUtils.isEmpty(testUser.getPassword()));
    }

    @Test
    public void testCreateUserModelViaCredentials() {
        Credentials testCredentials = new Credentials();
        testCredentials.setUin(aggieJackCredentials.getUin());
        testCredentials.setEmail(aggieJackCredentials.getEmail());
        testCredentials.setFirstName(aggieJackCredentials.getFirstName());
        testCredentials.setLastName(aggieJackCredentials.getLastName());
        testCredentials.setNetid(aggieJackCredentials.getNetid());
        testCredentials.setRole(aggieJackCredentials.getRole().toString());

        User testUser = new User(testCredentials);

        assertTrue("Test user name matches", testUser.getUsername().equals(aggieJackCredentials.getUin()));
        // TODO: requires fixes in the User model for these test.
        //assertTrue("Test user email matches", testUser.getEmail().equals(aggieJackCredentials.getEmail()));
        //assertTrue("Test user netid matches", testUser.getNetid().equals(aggieJackCredentials.getNetid()));
        assertTrue("Test user first name matches", testUser.getFirstName().equals(aggieJackCredentials.getFirstName()));
        assertTrue("Test user last name matches", testUser.getLastName().equals(aggieJackCredentials.getLastName()));
        assertTrue("Test user role matches", testUser.getRole().toString().equals(aggieJackCredentials.getRole()));
        assertTrue("Test user password is empty", StringUtils.isEmpty(testUser.getPassword()));
    }

    @Test
    public void testCreateUserModelViaParameters() {
        Role role = Role.valueOf(aggieJackCredentials.getRole());
        User testUser = new User(aggieJackCredentials.getEmail(), aggieJackCredentials.getFirstName(), aggieJackCredentials.getLastName(), role);

        assertTrue("Test user name is empty", StringUtils.isEmpty(testUser.getUsername()));
        assertTrue("Test user email matches", testUser.getEmail().equals(aggieJackCredentials.getEmail()));
        assertTrue("Test user first name matches", testUser.getFirstName().equals(aggieJackCredentials.getFirstName()));
        assertTrue("Test user last name matches", testUser.getLastName().equals(aggieJackCredentials.getLastName()));
        assertTrue("Test user netid is empty", StringUtils.isEmpty(testUser.getNetid()));
        assertTrue("Test user role matches", testUser.getRole().toString().equals(aggieJackCredentials.getRole()));
        assertTrue("Test user password is empty", StringUtils.isEmpty(testUser.getPassword()));
    }

    @Test
    public void testUserHasAuthorities() {
        User testUser = new User(aggieJackCredentials);

        assertTrue("Test user should have a single authority", testUser.getAuthorities().size() == 1);

        testUser.getAuthorities().forEach(authority -> {
            assertTrue("Test user should have authorities", authority.toString().equals(aggieJackCredentials.getRole().toString()));
        });
    }

    private Credentials getMockAggieJackCredentials() throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(aggieJackCredentialsResource.getFile(), Credentials.class);
    }

}
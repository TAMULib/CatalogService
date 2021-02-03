package edu.tamu.app.model.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.app.WebServerInit;
import edu.tamu.app.model.User;
import edu.tamu.weaver.auth.model.Credentials;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { WebServerInit.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
public class UserRepoTest {

    @Value("classpath:mock/credentials/aggiejack.json")
    private Resource aggieJackCredentialsResource;

    @Autowired
    private UserRepo userRepo;

    private Credentials aggieJackCredentials;

    @Spy
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        aggieJackCredentials = getMockAggieJackCredentials();

        userRepo.deleteAll();
    }
    
    @Test
    public void testFindUser() {
        User user = new User(aggieJackCredentials);

        userRepo.create(user);
        assertEquals("User repository is empty.", 1, userRepo.findAll().size());

        Optional<User> assertUser = userRepo.findByUsername(user.getUsername());
        assertEquals("Test User was not found.", assertUser.get().getUsername(), user.getUsername());
    }

    @Test
    public void testCreateUserModelViaCredentials() {
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
    public void testCreate() {
        User testUser = new User(aggieJackCredentials);

        userRepo.create(testUser);
        assertTrue("Test user was not added.", userRepo.findByUsername(testUser.getUsername()).isPresent());
    }

    @Test
    public void testDelete() {
        User testUser = new User(aggieJackCredentials);

        userRepo.create(testUser);
        Optional<User> addedUser = userRepo.findByUsername(testUser.getUsername());
        assertTrue("Test user was not added.", addedUser.isPresent());

        userRepo.delete(addedUser.get());
        assertEquals("Test user was not removed.", 0, userRepo.count());
    }

    @Test
    public void testMethod() {

        // Test create user
        User testUser = userRepo.create(new User(aggieJackCredentials));
        Optional<User> assertUser = userRepo.findByUsername("123456789");
        assertEquals("Test User1 was not added.", testUser.getUsername(), assertUser.get().getUsername());

        // Test disallow duplicate UINs
        userRepo.create(new User(aggieJackCredentials));
        List<User> allUsers = (List<User>) userRepo.findAll();
        assertEquals("Duplicate UIN found.", 1, allUsers.size());

        // Test delete user
        userRepo.delete(testUser);
        allUsers = (List<User>) userRepo.findAll();
        assertEquals("Test User1 was not removed.", 0, allUsers.size());

    }

    @Test
    public void testGetAuthorities() {
        User testUser1 = userRepo.create(new User(aggieJackCredentials));
        Collection<? extends GrantedAuthority> authorities = testUser1.getAuthorities();
        assertNotNull(authorities);
    }

    @Test
    public void testStaticUtilityMethods() {
        User testUser1 = userRepo.create(new User(aggieJackCredentials));
        assertEquals("Value was not false", false, testUser1.isAccountNonExpired());
        assertEquals("Value was not false", false, testUser1.isAccountNonLocked());
        assertEquals("Value was not false", false, testUser1.isCredentialsNonExpired());
        assertEquals("Value was not true", true, testUser1.isEnabled());
        assertEquals("Value was not null", null, testUser1.getPassword());
    }

    @After
    public void cleanUp() {
        userRepo.deleteAll();
    }

    private Credentials getMockAggieJackCredentials() throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(aggieJackCredentialsResource.getFile(), Credentials.class);
    }

}
package edu.tamu.catalog.controller;

import static edu.tamu.weaver.response.ApiStatus.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.catalog.model.User;
import edu.tamu.catalog.model.repo.UserRepo;
import edu.tamu.catalog.service.FolioCatalogService;
import edu.tamu.weaver.auth.model.Credentials;
import edu.tamu.weaver.response.ApiResponse;

@RunWith(SpringRunner.class)
public class UserControllerTest {

    @Value("classpath:mock/credentials/aggiejack.json")
    private Resource aggieJackCredentialsResource;

    @Mock
    private UserRepo userRepo;

    @Mock
    private FolioCatalogService folioCatalogService;

    @InjectMocks
    private UserController userController;

    private Credentials aggieJackCredentials;

    @Spy
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws JsonParseException, JsonMappingException, IOException {
        aggieJackCredentials = getMockAggieJackCredentials();
    }

    @Test
    public void testCredentialsSuccess() {
        ApiResponse response = userController.credentials(aggieJackCredentials);
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
    }

    @Test
    public void testAllUsersSuccess() {
        when(userRepo.findAll()).thenReturn(new ArrayList<User>());

        ApiResponse response = userController.allUsers();
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
        verify(userRepo).findAll();
    }

    @Test
    public void testUpdateUserSuccess() {
        User updatedUser = new User(aggieJackCredentials);

        when(userRepo.update(any(User.class))).thenReturn(updatedUser);

        ApiResponse response = userController.updateUser(updatedUser);
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
        verify(userRepo).update(any(User.class));
    }

    @Test
    public void testDeleteUserSuccess() {
        doNothing().when(userRepo).delete(any(User.class));

        ApiResponse response = userController.delete(new User(aggieJackCredentials));
        assertEquals("Did not receive expected successful response", SUCCESS, response.getMeta().getStatus());
        verify(userRepo).delete(any(User.class));
    }

    private Credentials getMockAggieJackCredentials() throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(aggieJackCredentialsResource.getFile(), Credentials.class);
    }

}

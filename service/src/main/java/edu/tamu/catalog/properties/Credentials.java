package edu.tamu.catalog.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Credentials {

    private String username;

    private String password;

    @JsonCreator
    public Credentials(
        @JsonProperty(value = "username", required = true) String username,
        @JsonProperty(value = "password", required = true) String password
    ) {
        setUsername(username);
        setPassword(password);
    }

}

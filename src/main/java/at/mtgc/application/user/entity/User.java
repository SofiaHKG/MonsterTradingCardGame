package at.mtgc.application.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String username;
    private String password;
    private String token;

    public User() {}

    @JsonCreator
    public User(
            @JsonProperty("Username") String username,
            @JsonProperty("Password") String password
    ) {
        this.username = username;
        this.password = password;
    }

    @JsonProperty("Username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("Username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("Password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("Password")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

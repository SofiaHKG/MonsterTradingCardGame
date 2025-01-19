package at.mtgc.application.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonAlias({"Username"})
    @JsonProperty("username")
    private String username;

    @JsonAlias({"Password"})
    @JsonProperty("password")
    private String password;

    private String token;
    private int coins;

    @JsonProperty("Name")
    private String fullname;

    @JsonProperty("Bio")
    private String bio;

    @JsonProperty("Image")
    private String image;

    public User() {}

    public User(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password
    ) {
        this.username = username;
        this.password = password;
        this.token = "";
        this.coins = 20;
    }

    public User(String username, String password, String token, int coins, String fullname, String bio, String image) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.coins = coins;
        this.fullname = fullname;
        this.bio = bio;
        this.image = image;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
package at.mtgc.application.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreboardEntry {
    @JsonProperty("username")
    private String username;
    @JsonProperty("wins")
    private int wins;
    @JsonProperty("losses")
    private int losses;
    @JsonProperty("elo")
    private int elo;

    public ScoreboardEntry(String username, int wins, int losses, int elo) {
        this.username = username;
        this.wins = wins;
        this.losses = losses;
        this.elo = elo;
    }

    public String getUsername() { return username; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getElo() { return elo; }
}

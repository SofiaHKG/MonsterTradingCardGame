package at.mtgc.application.packages.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    private String id;
    private String name;
    private double damage;

    @JsonCreator
    public Card(
            @JsonProperty("Id") String id,
            @JsonProperty("Name") String name,
            @JsonProperty("Damage") double damage
    ) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    @JsonProperty("Id")
    public String getId() {
        return id;
    }

    @JsonProperty("Id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("Name")
    public String getName() {
        return name;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("Damage")
    public double getDamage() {
        return damage;
    }

    @JsonProperty("Damage")
    public void setDamage(double damage) {
        this.damage = damage;
    }
}

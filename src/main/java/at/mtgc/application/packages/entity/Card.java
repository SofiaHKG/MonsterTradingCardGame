package at.mtgc.application.packages.entity;

import java.util.UUID;

public class Card {
    private UUID id;
    private String name;
    private double damage;

    public Card(UUID id, String name, double damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                '}';
    }
}

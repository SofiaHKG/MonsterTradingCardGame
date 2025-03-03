package at.mtgc.application.trading.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class TradingDeal {
    private UUID id;
    private UUID cardToTrade;
    private String type;
    private double minimumDamage;
    private String owner;

    @JsonCreator
    public TradingDeal(
            @JsonProperty("Id") String id,
            @JsonProperty("CardToTrade") String cardToTrade,
            @JsonProperty("Type") String type,
            @JsonProperty("MinimumDamage") double minimumDamage
    ) {
        this.id = UUID.fromString(id);
        this.cardToTrade = UUID.fromString(cardToTrade);
        this.type = type;
        this.minimumDamage = minimumDamage;
    }

    public TradingDeal() {}

    @JsonProperty("Id")
    public UUID getId() { return id; }
    @JsonProperty("Id")
    public void setId(UUID id) { this.id = id; }

    @JsonProperty("CardToTrade")
    public UUID getCardToTrade() { return cardToTrade; }
    @JsonProperty("CardToTrade")
    public void setCardToTrade(UUID cardToTrade) { this.cardToTrade = cardToTrade; }

    @JsonProperty("Type")
    public String getType() { return type; }
    @JsonProperty("Type")
    public void setType(String type) { this.type = type; }

    @JsonProperty("MinimumDamage")
    public double getMinimumDamage() { return minimumDamage; }
    @JsonProperty("MinimumDamage")
    public void setMinimumDamage(double minimumDamage) { this.minimumDamage = minimumDamage; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}

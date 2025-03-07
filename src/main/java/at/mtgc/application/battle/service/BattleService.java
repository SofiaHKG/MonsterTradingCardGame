package at.mtgc.application.battle.service;

import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.server.http.HttpException;
import at.mtgc.server.http.Status;
import at.mtgc.server.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BattleService {

    private static String waitingUser = null;
    private final UserRepository userRepository;

    public BattleService(UserRepository userRepo) {
        this.userRepository = userRepo;
    }

    // Initiating the battle
    public synchronized String initiateBattle(String username) {
        // Make sure user and deck exist
        User user = userRepository.getUserByUsername(username);
        if(user == null) {
            throw new HttpException(Status.NOT_FOUND, "User not found");
        }

        List<Card> deck = userRepository.getUserDeck(username);
        if(deck.size() < 4) {
            throw new HttpException(Status.FORBIDDEN, "You need 4 cards in your deck! Come back later...");
        }

        // Battle lobby
        if(waitingUser == null) {
            waitingUser = username;
            return "Waiting for an opponent...";
        } else if(waitingUser.equals(username)) {
            return "You are already in the lobby, waiting for an opponent...";
        } else {
            String opponent = waitingUser;
            waitingUser = null;
            return startBattle(opponent, username);
        }
    }

    // Start the battle
    private String startBattle(String user1, String user2) {
        // Load decks
        List<Card> deck1 = userRepository.getUserDeck(user1);
        List<Card> deck2 = userRepository.getUserDeck(user2);

        if(deck1.size() < 4 || deck2.size() < 4) {
            return "One or both players do not have a valid deck. Battle cancelled.";
        }

        // In-memory copies
        List<Card> p1Deck = new ArrayList<>(deck1);
        List<Card> p2Deck = new ArrayList<>(deck2);

        StringBuilder log = new StringBuilder();
        log.append("Battle start: Player 1 ").append(user1).append(" vs. Player 2 ").append(user2).append("\n");

        final int MAX_ROUNDS = 100;
        // Random Number Generator (rng)
        Random rng = new Random();

        for(int round=1; round <= MAX_ROUNDS; round++) {
            if(p1Deck.isEmpty() || p2Deck.isEmpty()) {
                break;
            }
            Card c1 = p1Deck.get(rng.nextInt(p1Deck.size()));
            Card c2 = p2Deck.get(rng.nextInt(p2Deck.size()));

            double dmg1 = getEffectiveDamage(c1, c2);
            double dmg2 = getEffectiveDamage(c2, c1);

            log.append("Round ").append(round)
                    .append(": ").append(c1.getName()).append("(").append(dmg1).append(")")
                    .append(" vs ")
                    .append(c2.getName()).append("(").append(dmg2).append(")... ");

            if(Math.abs(dmg1 - dmg2) < 0.0001) {
                log.append("Draw!\n");
            } else if(dmg1 > dmg2) {
                log.append(user1).append(" wins this round.\n");
                p2Deck.remove(c2);
                p1Deck.add(c2);

                // DB update: owner=user1, in_deck=true
                updateCardOwnerInDb(c2.getId(), user1);

            } else {
                log.append(user2).append(" wins this round.\n");
                p1Deck.remove(c1);
                p2Deck.add(c1);

                // DB update: owner=user2, in_deck=true
                updateCardOwnerInDb(c1.getId(), user2);
            }
        }

        boolean p1Empty = p1Deck.isEmpty();
        boolean p2Empty = p2Deck.isEmpty();

        if(!p1Empty && p2Empty) {
            log.append(user1).append(" is the ULTIMATE WINNER of the BATTLE!.\n");
            applyEloAndStats(user1, user2, true);
        } else if(p1Empty && !p2Empty) {
            log.append(user2).append(" is the ULTIMATE WINNER of the BATTLE!\n");
            applyEloAndStats(user2, user1, true);
        } else {
            log.append("The battle ended in a DRAW!\n");
            applyEloAndStats(user1, user2, false);
        }

        return log.toString();
    }

    // ELO and stats (wins, losses)
    private void applyEloAndStats(String winner, String loser, boolean hasWinner) {
        if(!hasWinner) {
            // draw means no ELO change
            return;
        }
        // winner, loser
        User w = userRepository.getUserByUsername(winner);
        User l = userRepository.getUserByUsername(loser);

        if(w == null || l == null) return;

        w.setWins(w.getWins() + 1);
        w.setElo(w.getElo() + 3);

        l.setLosses(l.getLosses() + 1);
        l.setElo(l.getElo() - 5);

        // Unique feature
        if(w.getWins() % 5 == 0) {
            // +10 coins as a reward
            w.setCoins(w.getCoins() + 10);
            System.out.println("User " + winner + " reached " +  w.getWins() + " wins and earned +10 coins!");
        }

        userRepository.updateUserStats(w);
        userRepository.updateUserStats(l);
    }

    // Damage calculation
    private double getEffectiveDamage(Card attacker, Card defender) {
        double damage = attacker.getDamage();
        String aName = attacker.getName().toLowerCase();
        String dName = defender.getName().toLowerCase();

        // Goblins are too afraid of Dragons to attack
        if(aName.contains("goblin") && dName.contains("dragon")) {
            return 0.0;
        }
        // Wizzard can control Orks so they are not able to damage them
        if(aName.contains("ork") && dName.contains("wizzard")) {
            return 0.0;
        }
        // The armor of Knights is so heavy that WaterSpells make them drown instantly
        if(aName.contains("knight") && dName.contains("waterspell")) {
            return 0.0;
        }
        // The Kraken is immune against spells
        if(aName.contains("spell") && dName.contains("kraken")) {
            return 0.0;
        }
        // The FireElves know dragons since they were little and can evade their attacks
        if(aName.contains("dragon") && dName.contains("fireelf")) {
            return 0.0;
        }

        // Element effect (only relevant if at least one spell card)
        boolean aSpell = aName.contains("spell");
        boolean dSpell = dName.contains("spell");
        if(aSpell || dSpell) {
            damage *= getElementMultiplier(aName, dName);
        }
        return damage;
    }

    // Element multiplier
    private double getElementMultiplier(String attackerName, String defenderName) {
        String aElem = getElement(attackerName);
        String dElem = getElement(defenderName);

        if(aElem.equals("water") && dElem.equals("fire")) {
            return 2.0;
        } else if(aElem.equals("fire") && dElem.equals("water")) {
            return 0.5;
        } else if(aElem.equals("fire") && dElem.equals("normal")) {
            return 2.0;
        } else if(aElem.equals("normal") && dElem.equals("fire")) {
            return 0.5;
        } else if(aElem.equals("normal") && dElem.equals("water")) {
            return 2.0;
        } else if(aElem.equals("water") && dElem.equals("normal")) {
            return 0.5;
        }
        return 1.0;
    }

    private String getElement(String cardName) {
        String lower = cardName.toLowerCase();
        if(lower.contains("water")) return "water";
        if(lower.contains("fire")) return "fire";
        return "normal";
    }

    // DB update owner + in_deck=TRUE
    private void updateCardOwnerInDb(String cardId, String newOwner) {
        String sql = "UPDATE cards SET owner = ?, in_deck = TRUE WHERE id = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newOwner);
            stmt.setObject(2, UUID.fromString(cardId));
            stmt.executeUpdate();
        } catch(SQLException e) {
            System.err.println("Error updating card owner: " + e.getMessage());
        }
    }
}
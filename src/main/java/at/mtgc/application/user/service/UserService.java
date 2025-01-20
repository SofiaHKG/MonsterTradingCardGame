package at.mtgc.application.user.service;

import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.user.entity.ScoreboardEntry;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(User user) {
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if(user != null && user.getPassword().equals(password)) {
            String token = username + "-mtcgToken";
            user.setToken(token);

            boolean updated = userRepository.updateToken(username, token);
            if(!updated) {
                System.out.println("Token not found");
            }
            return user;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public List<Card> getUserCards(String username) {
        System.out.println("Fetching cards for user in UserService: " + username); // Debug

        return userRepository.getUserCards(username);
    }

    public List<Card> getUserDeck(String username) {
        return userRepository.getUserDeck(username);
    }

    public boolean updateUserDeck(String username, List<String> cardIds) {
        System.out.println("Updating deck for user: " + username); // Debugging
        System.out.println("Received card IDs: " + cardIds); // Debugging

        // Control if cards belong to the user
        List<Card> userCards = userRepository.getUserCards(username);
        List<String> ownedCardIds = userCards.stream().map(Card::getId).toList();

        System.out.println("User owns cards: " + ownedCardIds); // Debugging

        for(String cardId : cardIds) {
            if(!ownedCardIds.contains(cardId)) {
                System.out.println("Card ID " + cardId + " does not belong to user " + username); // Debugging
                return false;
            }
        }

        return userRepository.updateUserDeck(username, cardIds);
    }

    public boolean updateUser(String username, User updatedUser) {
        return userRepository.updateUser(username, updatedUser);
    }

    public User getUserStats(String username) {
        return userRepository.getUserStats(username);
    }

    public List<ScoreboardEntry> getScoreboard() {
        return userRepository.getScoreboard();
    }

}

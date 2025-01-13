package at.mtgc.application.user.service;

import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.packages.entity.Card;

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
}

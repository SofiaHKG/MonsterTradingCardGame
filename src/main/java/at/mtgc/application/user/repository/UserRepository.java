package at.mtgc.application.user.repository;

import at.mtgc.application.user.entity.User;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final Map<String, User> users = new HashMap<>();

    public User findByUsername(String username) {
        return users.get(username);
    }

    public boolean save(User user) {
        if (users.containsKey(user.getUsername())) {
            return false;
        }
        users.put(user.getUsername(), user);
        return true;
    }
}

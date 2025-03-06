package unit.at.mtgc.application.user.service;

import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.application.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for UserService using a mocked UserRepository

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    private UserRepository userRepositoryMock;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepositoryMock = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepositoryMock);
    }

    @Test
    void testRegister_UserDoesNotExist_ShouldSucceed() {
        // ARRANGE
        User newUser = new User("testUser", "testPass");
        when(userRepositoryMock.save(newUser)).thenReturn(true);

        // ACT
        boolean result = userService.register(newUser);

        // ASSERT
        assertTrue(result, "Registration should succeed if userRepo returns true");
        verify(userRepositoryMock, times(1)).save(newUser);
    }

    @Test
    void testRegister_UserAlreadyExists_ShouldReturnFalse() {
        User existingUser = new User("existingUser", "pw");
        when(userRepositoryMock.save(existingUser)).thenReturn(false);

        boolean result = userService.register(existingUser);

        assertFalse(result, "If user already exists, register() should return false");
        verify(userRepositoryMock).save(existingUser);
    }

    @Test
    void testLogin_Success() {
        String username = "alice";
        String password = "secret";

        User dbUser = new User(username, password);
        when(userRepositoryMock.findByUsername(username)).thenReturn(dbUser);

        User result = userService.login(username, password);

        assertNotNull(result, "Login with correct password should yield a valid User object");
        assertEquals(username, result.getUsername(), "Username should match");
        verify(userRepositoryMock).findByUsername(username);
    }

    @Test
    void testLogin_WrongPassword_ShouldReturnNull() {
        String username = "bob";
        String correctPW = "pw123";

        when(userRepositoryMock.findByUsername(username)).thenReturn(new User(username, correctPW));

        User result = userService.login(username, "WRONG-PW");

        assertNull(result, "If password is wrong, we expect login() to return null");
        verify(userRepositoryMock).findByUsername(username);
    }

    @Test
    void testGetUserStats_ExistingUser() {
        String username = "statsUser";
        User userStatsFromDB = new User(username, 2, 3, 120);  // wins=2, losses=3, elo=120
        when(userRepositoryMock.getUserStats(username)).thenReturn(userStatsFromDB);

        User stats = userService.getUserStats(username);

        assertNotNull(stats);
        assertEquals(2, stats.getWins());
        assertEquals(3, stats.getLosses());
        assertEquals(120, stats.getElo());
        verify(userRepositoryMock).getUserStats(username);
    }

    @Test
    void testGetUserStats_NotFound() {
        when(userRepositoryMock.getUserStats("nobody")).thenReturn(null);

        User stats = userService.getUserStats("nobody");

        assertNull(stats, "If user doesn't exist, repository returns null => service returns null");
        verify(userRepositoryMock).getUserStats("nobody");
    }
}

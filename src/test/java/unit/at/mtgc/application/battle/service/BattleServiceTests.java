package unit.at.mtgc.application.battle.service;

import at.mtgc.application.battle.service.BattleService;
import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.user.entity.User;
import at.mtgc.application.user.repository.UserRepository;
import at.mtgc.server.http.HttpException;
import at.mtgc.server.http.Status;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

// Unit tests for the BattleService using Mockito

@ExtendWith(MockitoExtension.class)
class BattleServiceTests {

    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private BattleService battleService;

    @AfterEach
    void resetLobbyField() throws Exception {
        Field waitingUserField = BattleService.class.getDeclaredField("waitingUser");
        waitingUserField.setAccessible(true);
        waitingUserField.set(null, null);
    }

    @Test
    void testInitiateBattle_DeckTooSmall() {
        // If user has only 2 cards in deck...
        List<Card> smallDeck = List.of(
                new Card("00000000-0000-0000-0000-000000000001", "WaterGoblin", 10),
                new Card("11111111-1111-1111-1111-111111111111", "Dragon", 50)
        );
        User userObj = new User("tinyDeckUser", "pass");
        given(userRepositoryMock.getUserByUsername("tinyDeckUser")).willReturn(userObj);
        given(userRepositoryMock.getUserDeck("tinyDeckUser")).willReturn(smallDeck);

        HttpException ex = assertThrows(HttpException.class,
                () -> battleService.initiateBattle("tinyDeckUser"));

        assertEquals(Status.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("need 4 cards"));
    }

    @Test
    void testInitiateBattle_NoOneWaiting() {
        User userA = new User("UserA", "pw");
        given(userRepositoryMock.getUserByUsername("UserA")).willReturn(userA);
        given(userRepositoryMock.getUserDeck("UserA")).willReturn(createDeckOfFour());

        String result = battleService.initiateBattle("UserA");
        assertEquals("Waiting for an opponent...", result);
    }

    @Test
    void testInitiateBattle_UserAlreadyInLobby() {
        User userX = new User("userX", "pwX");
        given(userRepositoryMock.getUserByUsername("userX")).willReturn(userX);
        given(userRepositoryMock.getUserDeck("userX")).willReturn(createDeckOfFour());

        String firstCall = battleService.initiateBattle("userX");
        assertEquals("Waiting for an opponent...", firstCall);

        String secondCall = battleService.initiateBattle("userX");
        assertTrue(secondCall.contains("already in the lobby"));
    }

    @Test
    void testInitiateBattle_MatchFoundStartsBattle() {
        User cobelUser = new User("cobel", "pwCobel");
        given(userRepositoryMock.getUserByUsername("cobel")).willReturn(cobelUser);
        given(userRepositoryMock.getUserDeck("cobel")).willReturn(createDeckOfFour());

        // First user calls
        String firstResult = battleService.initiateBattle("cobel");
        assertEquals("Waiting for an opponent...", firstResult);

        User markUser = new User("mark", "pwMark");
        given(userRepositoryMock.getUserByUsername("mark")).willReturn(markUser);
        given(userRepositoryMock.getUserDeck("mark")).willReturn(createDeckOfFour());

        // Second user calls so battle starts
        String secondResult = battleService.initiateBattle("mark");

        assertTrue(secondResult.contains("Battle start: Player 1 cobel vs. Player 2 mark"),
                "Should mention 'Battle start' with cobel and mark");
    }

    private List<Card> createDeckOfFour() {
        List<Card> deck = new ArrayList<>();
        deck.add(new Card("10000000-0000-0000-0000-000000000001", "Goblin", 10.0));
        deck.add(new Card("20000000-0000-0000-0000-000000000002", "Dragon", 50.0));
        deck.add(new Card("30000000-0000-0000-0000-000000000003", "RegularSpell", 20.0));
        deck.add(new Card("40000000-0000-0000-0000-000000000004", "Ork", 45.0));
        return deck;
    }
}

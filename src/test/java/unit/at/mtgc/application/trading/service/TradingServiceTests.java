package unit.at.mtgc.application.trading.service;

import at.mtgc.application.trading.entity.TradingDeal;
import at.mtgc.application.trading.repository.TradingRepository;
import at.mtgc.application.trading.service.TradingService;
import at.mtgc.server.http.HttpException;
import at.mtgc.server.http.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

// Unit tests for TradingService using a mocked TradingRepository

@ExtendWith(MockitoExtension.class)
class TradingServiceTests {

    @Mock
    private TradingRepository tradingRepositoryMock;

    @InjectMocks
    private TradingService tradingService;

    @BeforeEach
    void setUp() {}

    @Test
    void testGetAllDeals_EmptyList() {
        given(tradingRepositoryMock.findAllDeals()).willReturn(Collections.emptyList());

        var deals = tradingService.getAllDeals();

        assertTrue(deals.isEmpty(), "Expected empty list of deals");
        verify(tradingRepositoryMock).findAllDeals();
    }

    @Test
    void testGetAllDeals_NonEmpty() {
        TradingDeal deal1 = new TradingDeal();
        deal1.setId(UUID.randomUUID());
        deal1.setOwner("someUser");
        deal1.setMinimumDamage(10.0);
        deal1.setType("monster");

        List<TradingDeal> mockDeals = List.of(deal1);
        given(tradingRepositoryMock.findAllDeals()).willReturn(mockDeals);

        var result = tradingService.getAllDeals();

        assertEquals(1, result.size());
        assertEquals("someUser", result.get(0).getOwner());
        verify(tradingRepositoryMock).findAllDeals();
    }

    @Test
    void testDeleteDeal_DealNotFound() {
        given(tradingRepositoryMock.findDealById(any(UUID.class))).willReturn(null);

        String fakeDealId = UUID.randomUUID().toString();
        HttpException ex = assertThrows(HttpException.class, () ->
                tradingService.deleteDeal("alice", fakeDealId));

        assertEquals(Status.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("Deal not found"));
        try {
            verify(tradingRepositoryMock, never()).deleteDeal(any(UUID.class));
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDeleteDeal_NotOwner() {
        TradingDeal deal = new TradingDeal();
        deal.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        deal.setOwner("bob");
        given(tradingRepositoryMock.findDealById(any(UUID.class))).willReturn(deal);

        HttpException ex = assertThrows(HttpException.class, () ->
                tradingService.deleteDeal("alice", "11111111-1111-1111-1111-111111111111"));

        assertEquals(Status.FORBIDDEN, ex.getStatus());
        assertTrue(ex.getMessage().contains("You are not the owner of this deal"));

        try {
            verify(tradingRepositoryMock, never()).deleteDeal(any(UUID.class));
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDeleteDeal_Success() throws SQLException {
        TradingDeal deal = new TradingDeal();
        deal.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        deal.setOwner("carol");
        given(tradingRepositoryMock.findDealById(any(UUID.class))).willReturn(deal);

        willDoNothing().given(tradingRepositoryMock).deleteDeal(any(UUID.class));

        tradingService.deleteDeal("carol", "22222222-2222-2222-2222-222222222222");

        verify(tradingRepositoryMock).deleteDeal(eq(UUID.fromString("22222222-2222-2222-2222-222222222222")));
    }
}

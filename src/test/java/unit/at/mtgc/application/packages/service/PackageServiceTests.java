package unit.at.mtgc.application.packages.service;

import at.mtgc.application.packages.entity.Card;
import at.mtgc.application.packages.entity.Package;
import at.mtgc.application.packages.repository.PackageRepository;
import at.mtgc.application.packages.service.PackageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for PackageService using a mocked PackageRepository

@ExtendWith(MockitoExtension.class)
class PackageServiceTests {

    @Mock
    private PackageRepository packageRepositoryMock;

    @InjectMocks
    private PackageService packageService;

    @BeforeEach
    void setUp() {
        // packageService = new PackageService(packageRepositoryMock);
    }

    @Test
    void testAddPackage_ShouldDelegateToRepository() {
        // ARRANGE
        Card card1 = new Card("00000000-0000-0000-0000-000000000000", "WaterGoblin", 10.0);
        Card card2 = new Card("11111111-1111-1111-1111-111111111111", "FireSpell", 25.0);
        Package pack = new Package(List.of(card1, card2));

        // ACT
        packageService.addPackage(pack);

        // ASSERT
        verify(packageRepositoryMock, times(1)).addPackage(pack);
    }

    @Test
    void testGetNextPackage_NoPackages_ShouldReturnNull() {
        when(packageRepositoryMock.getNextPackage()).thenReturn(null);

        Package result = packageService.getNextPackage();

        assertNull(result, "If no package is available, we expect null");
        verify(packageRepositoryMock).getNextPackage();
    }

    @Test
    void testGetNextPackage_HasPackage_ShouldReturnIt() {
        Card card = new Card("22222222-2222-2222-2222-222222222222", "Dragon", 50.0);
        Package fakePkg = new Package(List.of(card));
        when(packageRepositoryMock.getNextPackage()).thenReturn(fakePkg);

        Package result = packageService.getNextPackage();

        assertNotNull(result, "Should return the fake package from the repository");
        assertEquals(1, result.getCards().size(), "There is exactly one Card in the package");
        assertEquals("Dragon", result.getCards().get(0).getName());
        verify(packageRepositoryMock).getNextPackage();
    }

    @Test
    void testHasPackages_ReturnsFalse() {
        when(packageRepositoryMock.hasPackages()).thenReturn(false);

        boolean hasPkgs = packageService.hasPackages();

        assertFalse(hasPkgs, "Expected 'hasPackages' to be false");
        verify(packageRepositoryMock).hasPackages();
    }

    @Test
    void testHasPackages_ReturnsTrue() {
        when(packageRepositoryMock.hasPackages()).thenReturn(true);

        boolean hasPkgs = packageService.hasPackages();

        assertTrue(hasPkgs, "Expected 'hasPackages' to be true");
        verify(packageRepositoryMock).hasPackages();
    }

    @Test
    void testAcquirePackage_Success() {
        String username = "tester";
        when(packageRepositoryMock.acquirePackage(username)).thenReturn(true);

        boolean result = packageService.acquirePackage(username);

        assertTrue(result, "Should succeed if repository returns true");

        verify(packageRepositoryMock).acquirePackage(username);
    }

    @Test
    void testAcquirePackage_Fail() {
        String username = "noCoinsUser";
        when(packageRepositoryMock.acquirePackage(username)).thenReturn(false);

        boolean result = packageService.acquirePackage(username);

        assertFalse(result, "Acquire should fail if repository returns false");
        verify(packageRepositoryMock).acquirePackage(username);
    }
}

package com.ecommerce.wishlist.repository;

import com.ecommerce.wishlist.mock.WishlistMock;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
public class WishlistRepositoryTest {
    private static MongodExecutable mongodExecutable;

    @Autowired
    private WishlistRepository wishlistRepository;

    @BeforeAll
    public static void setUp() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        String bindIp = "localhost";
        int port = 12345;
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new de.flapdoodle.embed.mongo.config.Net(bindIp, port, Network.localhostIsIPv6()))
                .build();

        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
    }

    @AfterAll
    public static void tearDown() {
        mongodExecutable.stop();
    }

    @AfterEach
    public void cleanDataBase() {
        wishlistRepository.deleteAll();
    }

    @DisplayName("Given a valid product and wishlist"
            + " when the wishlist is created"
            + " then the wishlist and product details should match the expected values")
    @Test
    public void shouldCreateUserWishlistWithSuccess() {
        var product = WishlistMock.getProduct(1);
        var wishlist = WishlistMock.getWishlist(List.of(product));
        wishlist.setVersion(null);

        var wishlistSaved = wishlistRepository.save(wishlist);

        var productSaved = wishlistSaved.getProducts().get(0);

        assertEquals(wishlistSaved.getUserId(), wishlist.getUserId());
        assertEquals(wishlistSaved.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistSaved.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(0, wishlist.getVersion());
        assertEquals(productSaved.getProductId(), product.getProductId());
        assertEquals(productSaved.getProductName(), product.getProductName());
        assertEquals(productSaved.getQuantity(), product.getQuantity());
        assertEquals(productSaved.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productSaved.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productSaved.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productSaved.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a valid product and updated wishlist"
            + " when the updated wishlist is saved"
            + " then the saved wishlist should reflect the updates")
    @Test
    public void shouldUpdateUserWishlistWithSuccess() {
        var product = WishlistMock.getProduct(1);

        var product2 = WishlistMock.getProduct(1);
        product2.setProductId("12345");

        var wishlist = WishlistMock.getWishlist(List.of(product));
        wishlist.setVersion(null);

        var wishlistSaved = wishlistRepository.save(wishlist);

        wishlistSaved.setProducts(List.of(product, product2));

        wishlistSaved = wishlistRepository.save(wishlist);

        var firstProduct = wishlistSaved.getProducts().get(0);
        var secondProduct = wishlistSaved.getProducts().get(1);

        assertEquals(wishlistSaved.getUserId(), wishlist.getUserId());
        assertEquals(wishlistSaved.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistSaved.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(1, wishlist.getVersion());
        assertEquals(firstProduct.getProductId(), product.getProductId());
        assertEquals(firstProduct.getProductName(), product.getProductName());
        assertEquals(firstProduct.getQuantity(), product.getQuantity());
        assertEquals(firstProduct.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(firstProduct.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(firstProduct.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(firstProduct.getCreatedAt().getHour(), product.getCreatedAt().getHour());
        assertEquals(secondProduct.getProductId(), product2.getProductId());
        assertEquals(secondProduct.getProductName(), product2.getProductName());
        assertEquals(secondProduct.getQuantity(), product2.getQuantity());
        assertEquals(secondProduct.getUpdatedAt().toLocalDate(), product2.getUpdatedAt().toLocalDate());
        assertEquals(secondProduct.getUpdatedAt().getHour(), product2.getUpdatedAt().getHour());
        assertEquals(secondProduct.getCreatedAt().toLocalDate(), product2.getCreatedAt().toLocalDate());
        assertEquals(secondProduct.getCreatedAt().getHour(), product2.getCreatedAt().getHour());
    }

    @DisplayName("Given a saved wishlist in the repository"
                        + " when the wishlist is retrieved by ID"
                        + " then the retrieved wishlist and product details should match the expected values")
    @Test
    public void shouldBeReturnWishlistWithSuccess() {
        var product = WishlistMock.getProduct(1);
        var wishlist = WishlistMock.getWishlist(List.of(product));
        wishlist.setVersion(null);

        wishlistRepository.save(wishlist);

        var wishlistSaved = wishlistRepository.findById("123").get();
        var productSaved = wishlistSaved.getProducts().get(0);

        assertEquals(wishlistSaved.getUserId(), wishlist.getUserId());
        assertEquals(wishlistSaved.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistSaved.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(0, wishlist.getVersion());
        assertEquals(productSaved.getProductId(), product.getProductId());
        assertEquals(productSaved.getProductName(), product.getProductName());
        assertEquals(productSaved.getQuantity(), product.getQuantity());
        assertEquals(productSaved.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productSaved.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productSaved.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productSaved.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a saved wishlist in the repository"
            + " when the wishlist is retrieved by user ID and product ID"
            + " then the retrieved wishlist and product details should match the expected values")
    @Test
    public void ShouldBeReturnWishlistToUserAndProductWishSuccess() {
        var product = WishlistMock.getProduct(1);
        var wishlist = WishlistMock.getWishlist(List.of(product));
        wishlist.setVersion(null);

        wishlistRepository.save(wishlist);

        var wishlistSaved = wishlistRepository.findByUserIdAndProductId("123", "1234").get();
        var productSaved = wishlistSaved.getProducts().get(0);

        assertEquals(wishlistSaved.getUserId(), wishlist.getUserId());
        assertEquals(wishlistSaved.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistSaved.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistSaved.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(0, wishlist.getVersion());
        assertEquals(productSaved.getProductId(), product.getProductId());
        assertEquals(productSaved.getProductName(), product.getProductName());
        assertEquals(productSaved.getQuantity(), product.getQuantity());
        assertEquals(productSaved.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productSaved.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productSaved.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productSaved.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }
}

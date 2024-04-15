package com.ecommerce.wishlist.integration;

import com.ecommerce.wishlist.exception.ApiError;
import com.ecommerce.wishlist.mock.WishlistMock;
import com.ecommerce.wishlist.model.Product;
import com.ecommerce.wishlist.model.Wishlist;
import com.ecommerce.wishlist.repository.WishlistRepository;
import com.ecommerce.wishlist.response.ApiErrorResponse;
import com.ecommerce.wishlist.response.ValidationErrorResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishlistIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    ObjectMapper mapper = new ObjectMapper();

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

    @DisplayName("Given a valid wishlist and product request"
            + " when a post request is made to create a wishlist"
            + " then a new wishlist is successfully created and returned with the correct product details")
    @Test
    public void shouldCreateWishlistWithProductSuccessfully() {
        var productRequest = WishlistMock.getProductRequest(1);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        var response = restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class);

        var productExpected = WishlistMock.getProduct(1);
        var wishlistExpected = WishlistMock.getWishlist(List.of(productExpected));

        var wishlist = response.getBody();
        var product = wishlist.getProducts().stream().findFirst().get();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(wishlistExpected.getUserId(), wishlist.getUserId());
        assertEquals(wishlistExpected.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistExpected.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(productExpected.getProductId(), product.getProductId());
        assertEquals(productExpected.getProductName(), product.getProductName());
        assertEquals(productExpected.getQuantity(), product.getQuantity());
        assertEquals(productExpected.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productExpected.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productExpected.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productExpected.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a wishlist request with invalid data"
            + " when a post request is made"
            + " then it should return a BadRequest status with appropriate validation error messages")
    @Test
    public void shouldReturnBadRequestForInvalidWishlistRequest() {
        var productRequest = new ProductRequest();

        var wishlistRequest = new WishlistRequest(null, List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        var response = restTemplate.postForEntity("/api/wishlist", wishlistRequest, ValidationErrorResponse.class);

        var errors = List.of(
                "userId: User id cannot be empty",
                "products[0].productName: Product name cannot be empty",
                "products[0].quantity: Quantity cannot be null",
                "products[0].productId: Product id cannot be empty"
        );

        var errorExpected = new ValidationErrorResponse(
                ApiError.VALIDATION_ERROR,
                errors
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ApiError.VALIDATION_ERROR, errorDto.getType());
        assertTrue(errorDto.getErrors().containsAll(errorExpected.getErrors()));
    }

    @DisplayName("Given a duplicate wishlist creation attempt"
            + " when a post request is made"
            + " then it should return a Conflict status indicating the wishlist already exists")
    @Test
    public void shouldReturnConflictForDuplicateWishlistCreation() {
        var productRequest = WishlistMock.getProductRequest(1);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, Wishlist.class);

        var response = restTemplate.postForEntity("/api/wishlist", wishlistRequest, ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.WISHLIST_ALREADY_EXISTS,
        "Wishlist already exists to user: 123"
                );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given a wishlist request exceeding the maximum item limit"
            + " when a post request is made"
            + " then it should return an UnprocessableEntity status indicating the item limit was exceeded")
    @Test
    public void shouldReturnUnprocessableEntityForExceedingItemLimit() {
        var productRequest = WishlistMock.getProductRequest(10);
        var productRequest2 = WishlistMock.getProductRequest(11);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest, productRequest2));

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, Wishlist.class);

        var response = restTemplate.postForEntity("/api/wishlist", wishlistRequest, ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.MAX_ITEMS,
                "The total number of items on the wish list cannot exceed 20."
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given an invalid wishlist request for product addition"
            + " when a post request is made"
            + " then it should return a BadRequest status indicating validation errors for the product")
    @Test
    public void shouldReturnBadRequestForInvalidProductAddition() {
        var productRequest = WishlistMock.getProductRequest(10);
        var wishlistRequest = new WishlistRequest(
                null,
                List.of(productRequest)
        );

        mapper.registerModule(new JavaTimeModule());

        var response = restTemplate.postForEntity("/api/wishlist", wishlistRequest, Void.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @DisplayName("Given a valid wishlist and product request"
            + " when the product is retrieved by user ID"
            + " then the product details should match the expected values")
    @Test
    public void shouldRetrieveProductDetailsSuccessfully() {
        var productRequest = WishlistMock.getProductRequest(10);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        var wishlistExpected = restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class).getBody();
        var productExpected = wishlistExpected.getProducts().stream().findFirst().get();

        var response = restTemplate.getForEntity("/api/wishlist/123", WishlistResponse.class);
        var wishlist = response.getBody();
        var product = wishlist.getProducts().stream().findFirst().get();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wishlistExpected.getUserId(), wishlist.getUserId());
        assertEquals(wishlistExpected.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistExpected.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(productExpected.getProductId(), product.getProductId());
        assertEquals(productExpected.getProductName(), product.getProductName());
        assertEquals(productExpected.getQuantity(), product.getQuantity());
        assertEquals(productExpected.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productExpected.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productExpected.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productExpected.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a user ID and a product ID"
            + " when checked if the product is in the user's wishlist"
            + " then the product details are successfully returned if present in the wishlist")
    @Test
    public void shouldReturnProductIfIsInWishlist() {
        var productRequest = WishlistMock.getProductRequest(10);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        var wishlistExpected = restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class).getBody();
        var productExpected = wishlistExpected.getProducts().stream().findFirst().get();

        var response = restTemplate.getForEntity("/api/wishlist/123/products/1234", Product.class);
        var product = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productExpected.getProductId(), product.getProductId());
        assertEquals(productExpected.getProductName(), product.getProductName());
        assertEquals(productExpected.getQuantity(), product.getQuantity());
        assertEquals(productExpected.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productExpected.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productExpected.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productExpected.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given an invalid product ID for addition"
            + " when a post request is made"
            + " then it should return a BadRequest status with validation errors")
    @Test
    public void shouldReturnBadRequestForInvalidProductOnAddition() {
        var productRequest = new ProductRequest();
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class);

        var response = restTemplate.postForEntity("/api/wishlist/123/products", productRequest, ValidationErrorResponse.class);
        var errorResponse = response.getBody();

        var errors = List.of(
                "productName: Product name cannot be empty",
                "quantity: Quantity cannot be null",
                "productId: Product id cannot be empty"
        );

        var errorExpected = new ValidationErrorResponse(
                ApiError.VALIDATION_ERROR,
                errors
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ApiError.VALIDATION_ERROR, errorResponse.getType());
        assertTrue(errorResponse.getErrors().containsAll(errorExpected.getErrors()));
    }

    @DisplayName("Given a request for a non-existent product"
            + " when a get request is made for the product details"
            + " then it should return NotFound status indicating the product was not found")
    @Test
    public void shouldReturnNotFoundForNonExistentProduct() {
        var productRequest = WishlistMock.getProductRequest(10);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, Wishlist.class);

        var response = restTemplate.getForEntity("/api/wishlist/123/products/123", ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Product not found in wishlist of user: 123"
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given a non-existent product ID"
            + " when a product retrieval is attempted"
            + " then it should return NotFound status indicating the product was not found in the wishlist")
    @Test
    public void shouldReturnNotFoundForNonExistentProductInWishlist() {
        var response = restTemplate.getForEntity("/api/wishlist/123/products/1234", ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Product not found in wishlist of user: 123"
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given a valid product deletion request"
            + " when the product is deleted from the wishlist"
            + " then the operation should complete successfully")
    @Test
    public void shouldDeleteProductFromWishlistSuccessfully() {
        var productRequest = WishlistMock.getProductRequest(10);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class);

        var response = restTemplate.exchange("/api/wishlist/123/products/1234", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @DisplayName("Given a deletion request for a non-existent wishlist"
            + " when the deletion is attempted"
            + " then it should return NotFound status indicating the wishlist was not found")
    @Test
    public void shouldReturnNotFoundForNonExistentWishlistOnDeletion() {
        var response = restTemplate.exchange("/api/wishlist/123/products/1234", HttpMethod.DELETE, null, ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Wishlist not found to user: 123"
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given a user ID and a product to add"
            + " when the product is added to the user's wishlist"
            + " then the updated wishlist is successfully returned")
    @Test
    public void shouldAddProductToWishlistSuccessfully() {
        var productRequest = WishlistMock.getProductRequest(1);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        mapper.registerModule(new JavaTimeModule());

        var wishlistCreatedResponse = restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class);

        var response = restTemplate.postForEntity("/api/wishlist/123/products", productRequest, WishlistResponse.class);

        var wishlistExpected = wishlistCreatedResponse.getBody();
        var productExpected = wishlistExpected.getProducts().stream().findFirst().get();
        productExpected.setQuantity(2);

        var wishlist = response.getBody();
        var product = wishlist.getProducts().stream().findFirst().get();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wishlistExpected.getUserId(), wishlist.getUserId());
        assertEquals(wishlistExpected.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistExpected.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(productExpected.getProductId(), product.getProductId());
        assertEquals(productExpected.getProductName(), product.getProductName());
        assertEquals(productExpected.getQuantity(), product.getQuantity());
        assertEquals(productExpected.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productExpected.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productExpected.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productExpected.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a successful product addition to a wishlist"
            + " when another valid product is added"
            + " then the updated wishlist should reflect both products correctly")
    @Test
    public void shouldAddMultipleProductsToWishlistSuccessfully() {
        var productRequest = WishlistMock.getProductRequest(1);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        var productRequest2 = WishlistMock.getProductRequest(1);
        productRequest2.setProductId("12345");

        mapper.registerModule(new JavaTimeModule());

        var wishlistCreatedResponse = restTemplate.postForEntity("/api/wishlist", wishlistRequest, WishlistResponse.class);

        var response = restTemplate.postForEntity("/api/wishlist/123/products", productRequest2, WishlistResponse.class);

        var wishlistExpected = wishlistCreatedResponse.getBody();
        var firstProductExpected = wishlistExpected.getProducts().get(0);
        var secondProductExpected = WishlistMock.getProduct(1);
        secondProductExpected.setProductId("12345");

        var wishlist = response.getBody();
        var firstProduct = wishlist.getProducts().get(0);
        var secondProduct = wishlist.getProducts().get(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wishlistExpected.getUserId(), wishlist.getUserId());
        assertEquals(wishlistExpected.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(wishlistExpected.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(wishlistExpected.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(firstProductExpected.getProductId(), firstProduct.getProductId());
        assertEquals(firstProductExpected.getProductName(), firstProduct.getProductName());
        assertEquals(firstProductExpected.getQuantity(), firstProduct.getQuantity());
        assertEquals(firstProductExpected.getUpdatedAt().toLocalDate(), firstProduct.getUpdatedAt().toLocalDate());
        assertEquals(firstProductExpected.getUpdatedAt().getHour(), firstProduct.getUpdatedAt().getHour());
        assertEquals(firstProductExpected.getCreatedAt().toLocalDate(), firstProduct.getCreatedAt().toLocalDate());
        assertEquals(firstProductExpected.getCreatedAt().getHour(), firstProduct.getCreatedAt().getHour());
        assertEquals(secondProductExpected.getProductId(), secondProduct.getProductId());
        assertEquals(secondProductExpected.getProductName(), secondProduct.getProductName());
        assertEquals(secondProductExpected.getQuantity(), secondProduct.getQuantity());
        assertEquals(secondProductExpected.getUpdatedAt().toLocalDate(), secondProduct.getUpdatedAt().toLocalDate());
        assertEquals(secondProductExpected.getUpdatedAt().getHour(), secondProduct.getUpdatedAt().getHour());
        assertEquals(secondProductExpected.getCreatedAt().toLocalDate(), secondProduct.getCreatedAt().toLocalDate());
        assertEquals(secondProductExpected.getCreatedAt().getHour(), secondProduct.getCreatedAt().getHour());
    }

    @DisplayName("Given a product addition to an already existing wishlist"
            + " when a product that exceeds the item limit is added"
            + " then it should return UnprocessableEntity status indicating the item limit was exceeded")
    @Test
    public void shouldReturnUnprocessableEntityForAddingProductExceedingLimit() {
        var productRequest = WishlistMock.getProductRequest(2);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));

        var productRequest2 = WishlistMock.getProductRequest(20);
        productRequest2.setProductId("12345");

        mapper.registerModule(new JavaTimeModule());

        restTemplate.postForEntity("/api/wishlist", wishlistRequest, Wishlist.class);

        var response = restTemplate.postForEntity("/api/wishlist/123/products", productRequest2, ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.MAX_ITEMS,
                "The total number of items on the wish list cannot exceed 20."
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }

    @DisplayName("Given a product addition attempt to a non-existent wishlist"
            + " when the addition is attempted"
            + " then it should return NotFound status indicating the wishlist does not exist")
    @Test
    public void shouldReturnNotFoundForAddingToNonExistentWishlist() {
        var productRequest = WishlistMock.getProductRequest(2);
        var response = restTemplate.postForEntity("/api/wishlist/123/products", productRequest, ApiErrorResponse.class);

        var errorDtoExpected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Wishlist not found to user: 123"
        );

        var errorDto = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorDtoExpected, errorDto);
    }
}

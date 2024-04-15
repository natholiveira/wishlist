package com.ecommerce.wishlist.controller;

import com.ecommerce.wishlist.exception.AlreadyExistsException;
import com.ecommerce.wishlist.exception.ApiError;
import com.ecommerce.wishlist.exception.MaxItemsException;
import com.ecommerce.wishlist.exception.NotFoundException;
import com.ecommerce.wishlist.mock.WishlistMock;
import com.ecommerce.wishlist.response.ApiErrorResponse;
import com.ecommerce.wishlist.response.ProductResponse;
import com.ecommerce.wishlist.response.ValidationErrorResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;
import com.ecommerce.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
    }

    @DisplayName("Given a valid WishlistRequest"
            + " when a post request is made to create a wishlist"
            + " then a new wishlist is successfully created and returned")
    @Test
    public void shouldBeCreateUserWithSuccess() throws Exception {
        var productRequest = WishlistMock.getProductRequest(1);
        var wishlistRequest = WishlistMock.getWishlitRequest(List.of(productRequest));
        var mockProduct = WishlistMock.getProductResponse(1);
        var mockWishlist = WishlistMock.getWishlistResponse(List.of(mockProduct));
        var wishlistDtoJson = mapper.writeValueAsString(wishlistRequest);

        when(wishlistService.create(any())).thenReturn(mockWishlist);

        var result = mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wishlistDtoJson))
                .andExpect(status().isCreated())
                .andReturn();

        var wishlist = mapper.readValue(result.getResponse().getContentAsString(), WishlistResponse.class);
        assertEquals(wishlist, mockWishlist);

        verify(wishlistService, times(1)).create(wishlistRequest);
    }

    @DisplayName("Given an invalid WishlistRequest with null userId and empty product details"
            + " when a post request is made to create a wishlist"
            + " then it results in BadRequest due to validation errors")
    @Test
    public void shouldReturnBadRequestOnInvalidWishlistRequest() throws Exception {
        var productRequest = new ProductRequest(); // Request with default/empty values
        var wishlistRequest = new WishlistRequest(null, List.of(productRequest)); // Request with null userId
        var wishlistDtoJson = mapper.writeValueAsString(wishlistRequest);

        var result = mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wishlistDtoJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        var expectedErrors = List.of(
                "userId: User id cannot be empty",
                "products[0].productName: Product name cannot be empty",
                "products[0].quantity: Quantity cannot be null",
                "products[0].productId: Product id cannot be empty"
        );
        var expectedErrorResponse = new ValidationErrorResponse(ApiError.VALIDATION_ERROR, expectedErrors);
        var actualErrorResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationErrorResponse.class);

        assertEquals(ApiError.VALIDATION_ERROR, actualErrorResponse.getType());
        assertTrue(actualErrorResponse.getErrors().containsAll(expectedErrorResponse.getErrors()));

        verify(wishlistService, times(0)).create(wishlistRequest);
    }

    @DisplayName("Given a WishlistRequest for an existing user"
            + " when the create wishlist service is invoked"
            + " then it should return an AlreadyExists error")
    @Test
    public void shouldReturnErrorWhenUserAlreadyExists() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);
        var wishlistDto = WishlistMock.getWishlitRequest(List.of(productDto));
        var expected = new ApiErrorResponse(
                ApiError.WISHLIST_ALREADY_EXISTS,
                "Wishlist already exists to user: 123"
        );
        when(wishlistService.create(any()))
                .thenThrow(new AlreadyExistsException("Wishlist already exists to user: " + wishlistDto.getUserId()));
        var wishlistDtoJson = mapper.writeValueAsString(wishlistDto);

        var result = mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wishlistDtoJson))
                .andExpect(status().isConflict())
                .andReturn();

        var actualErrorResponse = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        assertEquals(expected, actualErrorResponse);

        verify(wishlistService, times(1)).create(wishlistDto);
    }

    @DisplayName("Given a WishlistRequest with the number of items exceeding the maximum limit"
            + " when the create wishlist service is invoked"
            + " then it should return a MaxItemsExceeded error")
    @Test
    public void shouldReturnErrorWhenMaxNumberExceed() throws Exception {
        var productDto = WishlistMock.getProductRequest(10);
        var productDto2 = WishlistMock.getProductRequest(11);
        var wishlistDto = WishlistMock.getWishlitRequest(List.of(productDto, productDto2));
        var expected = new ApiErrorResponse(
                ApiError.MAX_ITEMS,
                "The total number of items on the wish list cannot exceed 20."
        );

        when(wishlistService.create(any()))
                .thenThrow(new MaxItemsException("The total number of items on the wish list cannot exceed 20."));
        var wishlistDtoJson = mapper.writeValueAsString(wishlistDto);

        var result = mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wishlistDtoJson))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        var actualErrorResponse = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        assertEquals(expected, actualErrorResponse);

        verify(wishlistService, times(1)).create(wishlistDto);
    }

    @DisplayName("Given a user ID that corresponds to an existing wishlist"
            + " when the wishlist retrieval service is called"
            + " then the user's wishlist is successfully returned")
    @Test
    public void shouldReturnUserWishlistWithSuccess() throws Exception {
        var userId = "123";
        var mockProduct = WishlistMock.getProductResponse(1);
        var mockWishlist = WishlistMock.getWishlistResponse(List.of(mockProduct));

        when(wishlistService.getByUserId(anyString())).thenReturn(mockWishlist);

        var result = mockMvc.perform(get("/api/wishlist/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn();

        var actualWishlist = mapper.readValue(result.getResponse().getContentAsString(), WishlistResponse.class);
        assertEquals(mockWishlist, actualWishlist);

        verify(wishlistService, times(1)).getByUserId(userId);
    }

    @DisplayName("Given a user ID"
            + " when the wishlist retrieval service is called and the wishlist user is not found"
            + " then an error response is returned indicating not found user wishlist")
    @Test
    public void shouldReturnErrorWhenNotFoundUserWishlist() throws Exception {
        var userId = "123";

        when(wishlistService.getByUserId(anyString()))
                .thenThrow(new NotFoundException("Wishlist not found to user: "+userId));

        var expected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Wishlist not found to user: "+userId
        );

        var result = mockMvc.perform(get("/api/wishlist/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        var actualError = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        assertEquals(expected, actualError);

        verify(wishlistService, times(1)).getByUserId(userId);
    }

    @DisplayName("Given a user ID and a product ID"
            + " when checked if the product is in the user's wishlist"
            + " then the product details are successfully returned if present in the wishlist")
    @Test
    public void shouldReturnProductIfIsInWishlist() throws Exception {
        var userId = "user123";
        var productId = "product123";
        var mockProduct = WishlistMock.getProductResponse(1);

        when(wishlistService.isProductInWishlist(userId, productId)).thenReturn(mockProduct);

        var result = mockMvc.perform(get("/api/wishlist/{userId}/products/{productId}", userId, productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        var actualProduct = mapper.readValue(result.getResponse().getContentAsString(), ProductResponse.class);
        assertEquals(mockProduct, actualProduct);

        verify(wishlistService, times(1)).isProductInWishlist(userId, productId);
    }

    @DisplayName("Given a user ID and a product ID"
            + " when checked if the product is in the user's wishlist and the product is not found"
            + " then an error response is returned indicating the product is not in the wishlist")
    @Test
    public void shouldReturnErrorWhenNotFoundProductInWishlist() throws Exception {
        var userId = "user123";
        var productId = "product123";

        when(wishlistService.isProductInWishlist(anyString(), anyString()))
                .thenThrow(new NotFoundException("Product not found in wishlist of user: "+userId));

        var expected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Product not found in wishlist of user: "+userId
        );

        var result = mockMvc.perform(get("/api/wishlist/{userId}/products/{productId}", userId, productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        var actualError = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        assertEquals(expected, actualError);

        verify(wishlistService, times(1)).isProductInWishlist(userId, productId);
    }

    @DisplayName("Given a user ID and a product to add"
            + " when the product is added to the user's wishlist"
            + " then the updated wishlist is successfully returned")
    @Test
    public void shouldReturnSuccessWhenAddProductInWishlist() throws Exception {
        var userId = "user123";
        var productDto = WishlistMock.getProductRequest(1);
        var product = WishlistMock.getProductResponse(1);
        var mockWishlist = WishlistMock.getWishlistResponse(new ArrayList<>(List.of(product)));

        when(wishlistService.addProduct(anyString(), any())).thenReturn(mockWishlist);
        var productDtoJson = mapper.writeValueAsString(productDto);

        var result = mockMvc.perform(post("/api/wishlist/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productDtoJson))
                .andExpect(status().isOk())
                .andReturn();

        var wishlist = mapper.readValue(result.getResponse().getContentAsString(), WishlistResponse.class);
        assertEquals(mockWishlist, wishlist);

        verify(wishlistService, times(1)).addProduct(userId, productDto);
    }

    @DisplayName("Given a request with invalid product details"
            + " when a post request is made to add a product to the wishlist"
            + " then a BadRequest status is returned due to validation errors")
    @Test
    public void shouldReturnBadRequestWhenProductDetailsAreInvalid() throws Exception {
        var userId = "user123";
        var productRequest = new ProductRequest();  // Assuming empty details are set up by default in ProductRequest.
        var wishlistRequest = new WishlistRequest(null, List.of(productRequest));
        var wishlistDtoJson = mapper.writeValueAsString(wishlistRequest);

        var result = mockMvc.perform(post("/api/wishlist/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wishlistDtoJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        var expectedErrors = List.of(
                "productName: Product name cannot be empty",
                "quantity: Quantity cannot be null",
                "productId: Product id cannot be empty"
        );
        var expectedErrorResponse = new ValidationErrorResponse(ApiError.VALIDATION_ERROR, expectedErrors);
        var actualErrorResponse = mapper.readValue(result.getResponse().getContentAsString(), ValidationErrorResponse.class);

        assertEquals(ApiError.VALIDATION_ERROR, actualErrorResponse.getType());
        assertTrue(actualErrorResponse.getErrors().containsAll(expectedErrorResponse.getErrors()));

        verify(wishlistService, times(0)).create(wishlistRequest);
    }


    @DisplayName("Given a user ID and a product for a user without a wishlist"
            + " when attempting to add a product"
            + " then an error is returned indicating the wishlist was not found")
    @Test
    public void shouldReturnErrorWhenNotFoundUserWishlistOnAdd() throws Exception {
        var userId = "user123";
        var productDto = WishlistMock.getProductRequest(1);
        when(wishlistService.addProduct(anyString(), any()))
                .thenThrow(new NotFoundException("Wishlist not found to user: " + userId));

        var productDtoJson = mapper.writeValueAsString(productDto);

        var result = mockMvc.perform(post("/api/wishlist/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productDtoJson))
                .andExpect(status().isNotFound())
                .andReturn();

        var expected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Wishlist not found to user: "+userId
        );
        var actualError = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);

        assertEquals(expected, actualError);

        verify(wishlistService, times(1)).addProduct(userId, productDto);
    }

    @DisplayName("Given a user ID and a product that would exceed the wishlist's item limit"
            + " when attempting to add the product to the wishlist"
            + " then an error is returned indicating the maximum number of items has been exceeded")
    @Test
    public void shouldReturnErrorWhenMaxNumberExceedOnAdd() throws Exception {
        var userId = "user123";
        var productDto = WishlistMock.getProductRequest(1);
        when(wishlistService.addProduct(anyString(), any()))
                .thenThrow(new MaxItemsException("The total number of items on the wish list cannot exceed 20."));

        var productDtoJson = mapper.writeValueAsString(productDto);

        var result = mockMvc.perform(post("/api/wishlist/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productDtoJson))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        var expected = new ApiErrorResponse(
                ApiError.MAX_ITEMS,
                "The total number of items on the wish list cannot exceed 20."
        );
        var actualError = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);

        assertEquals(expected, actualError);

        verify(wishlistService, times(1)).addProduct(userId, productDto);
    }

    @DisplayName("Given a user ID and a product ID"
            + " when the product is removed from the user's wishlist"
            + " then the operation completes successfully with an OK status")
    @Test
    public void shouldReturnSuccessWhenRemoveProductInWishlist() throws Exception {
        var userId = "user123";
        var productId = "product123";
        doNothing().when(wishlistService).removeProduct(any(), any());

        mockMvc.perform(delete("/api/wishlist/{userId}/products/{productId}", userId, productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(wishlistService, times(1)).removeProduct(userId, productId);
    }

    @DisplayName("Given a user ID and a product ID for a non-existent wishlist"
            + " when attempting to remove a product from the wishlist"
            + " then an error is returned indicating that the wishlist was not found")
    @Test
    public void shouldReturnErrorWhenNotFoundUserWishlistOnRemove() throws Exception {
        var userId = "user123";
        var productId = "product123";
        doThrow(new NotFoundException("Wishlist not found to user: " + userId))
                .when(wishlistService).removeProduct(any(), any());

        var expected = new ApiErrorResponse(
                ApiError.NOT_FOUND,
                "Wishlist not found to user: "+userId
        );

        var result = mockMvc.perform(delete("/api/wishlist/{userId}/products/{productId}", userId, productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        var actualError = mapper.readValue(result.getResponse().getContentAsString(), ApiErrorResponse.class);
        assertEquals(expected, actualError);

        // Verify
        verify(wishlistService, times(1)).removeProduct(userId, productId);
    }
}

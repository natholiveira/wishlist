package com.ecommerce.wishlist.service;

import com.ecommerce.wishlist.exception.AlreadyExistsException;
import com.ecommerce.wishlist.exception.MaxItemsException;
import com.ecommerce.wishlist.exception.NotFoundException;
import com.ecommerce.wishlist.mock.WishlistMock;
import com.ecommerce.wishlist.repository.WishlistRepository;
import com.ecommerce.wishlist.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    private WishlistServiceImpl wishlistService;

    @Value("${wishlist.max.items}")
    private int maxItem = 20;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        wishlistService = new WishlistServiceImpl(wishlistRepository, maxItem);
    }

    @DisplayName("Given a valid wishlist request"
            + " when the wishlist is created"
            + " then the created wishlist and product details should match the expected values")
    @Test
    public void ShoudBeCreateUserWishlistWithSuccess() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);
        var wishlistDto = WishlistMock.getWishlitRequest(List.of(productDto));

        var mockProduct = WishlistMock.getProduct(1);
        var mockWishlist = WishlistMock.getWishlist(List.of(mockProduct));

        var productExpected = WishlistMock.getProductResponse(1);
        var wishlistExpected = WishlistMock.getWishlistResponse(List.of(productExpected));

        when(wishlistRepository.findById(any())).thenReturn(Optional.empty());

        when(wishlistRepository.save(any())).thenReturn(mockWishlist);

        var wishlist = wishlistService.create(wishlistDto);
        var product = wishlist.getProducts().get(0);

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

    @DisplayName("Given a wishlist request with existing user ID"
            + " when trying to create the wishlist"
            + " then an AlreadyExistsException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenUserAlreadyExists() {
        var productDto = WishlistMock.getProductRequest(1);

        var wishlistDto = WishlistMock.getWishlitRequest(List.of(productDto));

        var wishlist = WishlistMock.getWishlist(new ArrayList<>());

        when(wishlistRepository.findById(any())).thenReturn(Optional.of(wishlist));

        assertThrowsExactly(AlreadyExistsException.class, () -> wishlistService.create(wishlistDto));

        verify(wishlistRepository, times(0)).save(any());
    }

    @DisplayName("Given a wishlist request with too many products"
            + " when trying to create the wishlist"
            + " then a MaxItemsException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenMaxNumberExceed() {
        var productDto = WishlistMock.getProductRequest(10);
        var productDto2 = WishlistMock.getProductRequest(11);

        var wishlistDto = WishlistMock.getWishlitRequest(List.of(productDto, productDto2));

        when(wishlistRepository.findById(any())).thenReturn(Optional.empty());

        assertThrowsExactly(MaxItemsException.class, () -> wishlistService.create(wishlistDto));

        verify(wishlistRepository, times(0)).save(any());
    }

    @DisplayName("Given a valid user ID"
            + " when retrieving the wishlist"
            + " then the retrieved wishlist and product details should match the expected values")
    @Test
    public void ShoudBeReturnUserWishlistWithSuccess() throws Exception {
        var mockProduct = WishlistMock.getProduct(1);
        var mockWishlist = WishlistMock.getWishlist(List.of(mockProduct));

        var expectedProduct = WishlistMock.getProductResponse(1);
        var expectedWishlist = WishlistMock.getWishlistResponse(List.of(expectedProduct));

        when(wishlistRepository.findById(any())).thenReturn(Optional.of(mockWishlist));

        var wishlist = wishlistService.getByUserId("123");
        var product = wishlist.getProducts().get(0);

        assertEquals(expectedWishlist.getUserId(), wishlist.getUserId());
        assertEquals(expectedWishlist.getCreatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(expectedWishlist.getCreatedAt().getHour(), wishlist.getCreatedAt().getHour());
        assertEquals(expectedWishlist.getUpdatedAt().toLocalDate(), wishlist.getCreatedAt().toLocalDate());
        assertEquals(expectedWishlist.getUpdatedAt().getHour(), wishlist.getUpdatedAt().getHour());
        assertEquals(expectedProduct.getProductId(), product.getProductId());
        assertEquals(expectedProduct.getProductName(), product.getProductName());
        assertEquals(expectedProduct.getQuantity(), product.getQuantity());
        assertEquals(expectedProduct.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(expectedProduct.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(expectedProduct.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(expectedProduct.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a non-existent user ID"
            + " when retrieving the wishlist"
            + " then a NotFoundException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenNotFoundUser() throws Exception {
        when(wishlistRepository.findById(any())).thenReturn(Optional.empty());

        assertThrowsExactly(NotFoundException.class, () -> wishlistService.getByUserId("123"));
    }

    @DisplayName("Given a valid user ID and product ID"
            + " when checking if the product is in the wishlist"
            + " then the retrieved product details should match the expected values")
    @Test
    public void ShoudBeReturnProductIfIsInWishlist() throws Exception {
        var mockProduct = WishlistMock.getProduct(1);
        var mockProduct2 = WishlistMock.getProduct(2);
        mockProduct2.setProductId("234");
        var mockWishlist = WishlistMock.getWishlist(List.of(mockProduct, mockProduct2));

        var productExpected = WishlistMock.getProductResponse(2);
        productExpected.setProductId("234");

        when(wishlistRepository.findByUserIdAndProductId(anyString(), anyString())).thenReturn(Optional.of(mockWishlist));

        var product = wishlistService.isProductInWishlist("123", "234");

        assertEquals(productExpected.getProductId(), product.getProductId());
        assertEquals(productExpected.getProductName(), product.getProductName());
        assertEquals(productExpected.getQuantity(), product.getQuantity());
        assertEquals(productExpected.getUpdatedAt().toLocalDate(), product.getUpdatedAt().toLocalDate());
        assertEquals(productExpected.getUpdatedAt().getHour(), product.getUpdatedAt().getHour());
        assertEquals(productExpected.getCreatedAt().toLocalDate(), product.getCreatedAt().toLocalDate());
        assertEquals(productExpected.getCreatedAt().getHour(), product.getCreatedAt().getHour());
    }

    @DisplayName("Given a valid user ID and non-existent product ID"
            + " when checking if the product is in the wishlist"
            + " then a NotFoundException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenNotFoundProductInWishList() throws Exception {
        when(wishlistRepository.findByUserIdAndProductId(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrowsExactly(NotFoundException.class, () -> wishlistService.isProductInWishlist("123", "1234"));
    }

    @DisplayName("Given a valid user ID and existing product ID"
            + " when removing the product from the wishlist"
            + " then the product should be successfully removed")
    @Test
    public void ShoudBeReturnSuccessWhenRemoveProductOfWishlist() throws Exception {
        var product = WishlistMock.getProduct(1);
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));

        wishlistService.removeProduct("123", "1234");

        mockWishlist.setProducts(new ArrayList<>());

        verify(wishlistRepository, times(1)).save(mockWishlist);
    }

    @DisplayName("Given a valid user ID and existing product ID"
            + " when decrementing the product quantity in the wishlist"
            + " then the product quantity should be successfully decremented")
    @Test
    public void ShoudBeReturnSuccessWhenDecrementProductOfWishlist() throws Exception {
        var product = WishlistMock.getProduct(2);
        var product2 = WishlistMock.getProduct(1);
        product2.setProductId("12345");
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product, product2)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));

        wishlistService.removeProduct("123", "1234");

        mockWishlist.getProducts().get(0).setQuantity(1);

        verify(wishlistRepository, times(1)).save(mockWishlist);
    }

    @DisplayName("Given a valid user ID and non-existent product ID"
            + " when trying to remove the product from the wishlist"
            + " then a NotFoundException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenNotFoundProductInWishListOnRemove() throws Exception {
        var product = WishlistMock.getProduct(1);
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));

        assertThrowsExactly(NotFoundException.class, () -> wishlistService.removeProduct("123", "234"));

        verify(wishlistRepository, times(0)).save(any());
    }

    @DisplayName("Given a non-existent user ID"
            + " when trying to remove the product from the wishlist"
            + " then a NotFoundException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenNotUserWishListOnRemove() throws Exception {

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrowsExactly(NotFoundException.class, () -> wishlistService.removeProduct("123", "1234"));

        verify(wishlistRepository, times(0)).save(any());
    }

    @DisplayName("Given a valid user ID and product request"
            + " when trying to increment the product quantity in the wishlist"
            + " then the product quantity should be successfully incremented")
    @Test
    public void ShoudBeReturnSuccessWhenIncrementProductOfWishlist() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);

        var product = WishlistMock.getProduct(2);
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));
        when(wishlistRepository.save(any())).thenReturn(mockWishlist);

        wishlistService.addProduct("123", productDto);

        mockWishlist.getProducts().get(0).setQuantity(3);

        verify(wishlistRepository, times(1)).save(mockWishlist);
    }

    @DisplayName("Given a valid user ID and product request with existing product ID"
                        + " when adding the product to the wishlist"
                        + " then the product should be successfully added")
    @Test
    public void ShoudBeReturnSuccessWhenAddProductInWishlist() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);
        productDto.setProductId("234");

        var product = WishlistMock.getProduct(1);
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));
        when(wishlistRepository.save(any())).thenReturn(mockWishlist);

        wishlistService.addProduct("123", productDto);

        verify(wishlistRepository, times(1)).save(mockWishlist);
    }

    @DisplayName("Given a non-existent user ID"
            + " when trying to add the product to the wishlist"
            + " then a NotFoundException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenNotUserWishListOnAdd() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrowsExactly(NotFoundException.class, () -> wishlistService.addProduct("123", productDto));

        verify(wishlistRepository, times(0)).save(any());
    }

    @DisplayName("Given a valid user ID and product request with too many products"
            + " when trying to add the product to the wishlist"
            + " then a MaxItemsException should be thrown")
    @Test
    public void ShoudBeReturnErrorWhenMaxNumberExceedOnAdd() throws Exception {
        var productDto = WishlistMock.getProductRequest(1);
        productDto.setProductId("234");

        var product = WishlistMock.getProduct(20);
        var mockWishlist = WishlistMock.getWishlist(new ArrayList<>(List.of(product)));

        when(wishlistRepository.findById(anyString())).thenReturn(Optional.of(mockWishlist));

        assertThrowsExactly(MaxItemsException.class, () -> wishlistService.addProduct("123", productDto));

        verify(wishlistRepository, times(0)).save(any());
    }
}

package com.ecommerce.wishlist.mock;

import com.ecommerce.wishlist.response.ProductResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;
import com.ecommerce.wishlist.model.Product;
import com.ecommerce.wishlist.model.Wishlist;

import java.time.LocalDateTime;
import java.util.List;

public class WishlistMock {

    public static Wishlist getWishlist(List<Product> products) {
        return new Wishlist(
                "123",
                LocalDateTime.now(),
                LocalDateTime.now(),
                products,
                0L
        );
    }

    public static Product getProduct(int quantity) {
        return new Product(
                "1234",
                "teste",
                quantity,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static WishlistResponse getWishlistResponse(List<ProductResponse> products) {
        return new WishlistResponse(
                "123",
                LocalDateTime.now(),
                LocalDateTime.now(),
                products
        );
    }

    public static ProductResponse getProductResponse(int quantity) {
        return new ProductResponse(
                "1234",
                "teste",
                quantity,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static WishlistRequest getWishlitRequest(List<ProductRequest> products) {
        return new WishlistRequest(
                "123",
                products
        );
    }

    public static ProductRequest getProductRequest(int quantity) {
        return new ProductRequest(
                "1234",
                "teste",
                quantity
        );
    }
}

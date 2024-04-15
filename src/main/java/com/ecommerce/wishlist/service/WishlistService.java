package com.ecommerce.wishlist.service;

import com.ecommerce.wishlist.exception.AlreadyExistsException;
import com.ecommerce.wishlist.exception.MaxItemsException;
import com.ecommerce.wishlist.exception.NotFoundException;
import com.ecommerce.wishlist.response.ProductResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;

public interface WishlistService {
    WishlistResponse create(WishlistRequest wishlistRequest) throws MaxItemsException, AlreadyExistsException;

    WishlistResponse addProduct(String userId, ProductRequest productRequest) throws MaxItemsException, NotFoundException;

    WishlistResponse getByUserId(String userId) throws NotFoundException;

    ProductResponse isProductInWishlist(String userId, String productId) throws NotFoundException;

    void removeProduct(String userId, String productId) throws NotFoundException;
}

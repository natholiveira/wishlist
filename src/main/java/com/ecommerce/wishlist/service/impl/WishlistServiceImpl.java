package com.ecommerce.wishlist.service.impl;

import com.ecommerce.wishlist.response.ProductResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;
import com.ecommerce.wishlist.exception.AlreadyExistsException;
import com.ecommerce.wishlist.exception.MaxItemsException;
import com.ecommerce.wishlist.exception.NotFoundException;
import com.ecommerce.wishlist.model.Product;
import com.ecommerce.wishlist.model.Wishlist;
import com.ecommerce.wishlist.repository.WishlistRepository;
import com.ecommerce.wishlist.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;

    private final int maxItem;

    @Autowired
    public WishlistServiceImpl(WishlistRepository wishlistRepository,  @Value("${wishlist.max.items:20}") int maxItem) {
        this.wishlistRepository = wishlistRepository;
        this.maxItem = maxItem;
    }

    @Override
    public WishlistResponse create(WishlistRequest wishlistRequest) throws MaxItemsException, AlreadyExistsException {
        var existingWishlist = wishlistRepository.findById(wishlistRequest.getUserId());
        if (existingWishlist.isPresent()) {
            throw new AlreadyExistsException("Wishlist already exists to user: " + wishlistRequest.getUserId());
        }

        var wishlist = Wishlist.fromRequest(wishlistRequest);
        checkTotalQuantity(wishlist);

        wishlist = wishlistRepository.save(wishlist);

        return WishlistResponse.fromWishlist(wishlist);
    }

    @Override
    public WishlistResponse addProduct(String userId, ProductRequest productRequest) throws MaxItemsException, NotFoundException {
        var wishlist = wishlistRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Wishlist not found to user: " + userId));

        var optionalProduct = wishlist.getProducts().stream()
                .filter(p -> p.getProductId().equals(productRequest.getProductId()))
                .findFirst();

        if (optionalProduct.isPresent()) {
            var product = optionalProduct.get();
            product.setQuantity(product.getQuantity() + productRequest.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());
        } else {
            wishlist.getProducts().add(Product.fromRequest(productRequest));
        }

        checkTotalQuantity(wishlist);

        wishlist = wishlistRepository.save(wishlist);

        return WishlistResponse.fromWishlist(wishlist);
    }

    @Override
    public WishlistResponse getByUserId(String userId) throws NotFoundException {
         var wishlist = wishlistRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Wishlist not found to user: " + userId));

         return WishlistResponse.fromWishlist(wishlist);
    }

    @Override
    public ProductResponse isProductInWishlist(String userId, String productId) throws NotFoundException {
        var wishlist =  wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found in wishlist of user: "+userId));

        return wishlist.getProducts().stream()
                .filter(product -> product.getProductId().equals(productId))
                .findFirst()
                .map(ProductResponse::fromProduct)
                .orElseThrow(() -> new NotFoundException("Product not found in wishlist of user: " + userId));
    }

    @Override
    public void removeProduct(String userId, String productId) throws NotFoundException {
        var wishlist = wishlistRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Wishlist not found to user: " + userId));

        var productOpt = wishlist.getProducts().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found in wishlist!"));

        if (productOpt.getQuantity() > 1) {
            productOpt.setQuantity(productOpt.getQuantity() - 1);
            productOpt.setUpdatedAt(LocalDateTime.now());
        } else {
            wishlist.getProducts().remove(productOpt);
        }

        wishlistRepository.save(wishlist);
    }

    private void checkTotalQuantity(Wishlist wishlist) throws MaxItemsException {
        int totalQuantity = wishlist.getProducts().stream()
                .mapToInt(Product::getQuantity)
                .sum();

        if (totalQuantity > maxItem) {
            throw new MaxItemsException("The total number of items on the wish list cannot exceed " + maxItem + ".");
        }
    }
}

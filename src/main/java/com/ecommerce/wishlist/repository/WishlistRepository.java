package com.ecommerce.wishlist.repository;

import com.ecommerce.wishlist.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    @Query("{'userId': ?0, 'products.productId': ?1}")
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);
}

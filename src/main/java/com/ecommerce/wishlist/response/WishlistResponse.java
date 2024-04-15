package com.ecommerce.wishlist.response;

import com.ecommerce.wishlist.model.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistResponse {

    String userId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    List<ProductResponse> products;

    public static WishlistResponse fromWishlist(Wishlist wishlist) {
        return new WishlistResponse(
                wishlist.getUserId(),
                wishlist.getCreatedAt(),
                wishlist.getUpdatedAt(),
                wishlist.getProducts().stream().map(ProductResponse::fromProduct).collect(Collectors.toList())
        );
    }
}

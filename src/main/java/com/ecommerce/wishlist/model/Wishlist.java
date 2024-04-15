package com.ecommerce.wishlist.model;

import com.ecommerce.wishlist.resquest.WishlistRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("wishlist")
public class Wishlist {

    @Id
    String userId;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    List<Product> products;

    @Version
    private Long version;

    public static Wishlist fromRequest(WishlistRequest request) {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(request.getUserId());
        wishlist.setProducts(request.getProducts().stream().map(Product::fromRequest).collect(Collectors.toList()));
        return wishlist;
    }
}

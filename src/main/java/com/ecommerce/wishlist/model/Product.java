package com.ecommerce.wishlist.model;

import com.ecommerce.wishlist.resquest.ProductRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    String productId;
    String productName;
    Integer quantity;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static Product fromRequest(ProductRequest request) {
        return new Product(
                request.getProductId(),
                request.getProductName(),
                request.getQuantity(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

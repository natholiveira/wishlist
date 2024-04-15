package com.ecommerce.wishlist.response;

import com.ecommerce.wishlist.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    String productId;
    String productName;
    Integer quantity;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static ProductResponse fromProduct(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
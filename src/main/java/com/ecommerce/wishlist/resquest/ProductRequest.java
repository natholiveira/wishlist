package com.ecommerce.wishlist.resquest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotEmpty(message = "Product id cannot be empty")
    String productId;

    @NotEmpty(message = "Product name cannot be empty")
    String productName;

    @NotNull(message = "Quantity cannot be null")
    Integer quantity;
}

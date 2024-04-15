package com.ecommerce.wishlist.resquest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistRequest {
    @NotEmpty(message = "User id cannot be empty")
    String userId;

    @Valid
    List<ProductRequest> products;
}

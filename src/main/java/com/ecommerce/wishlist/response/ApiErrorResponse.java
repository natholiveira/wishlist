package com.ecommerce.wishlist.response;

import com.ecommerce.wishlist.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private ApiError type;
    private String message;
}
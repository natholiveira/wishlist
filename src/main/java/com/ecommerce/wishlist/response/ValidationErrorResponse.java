package com.ecommerce.wishlist.response;

import com.ecommerce.wishlist.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private ApiError type;
    private List<String> errors;
}
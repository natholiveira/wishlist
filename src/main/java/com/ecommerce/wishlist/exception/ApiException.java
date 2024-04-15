package com.ecommerce.wishlist.exception;

import com.ecommerce.wishlist.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends Exception {

    ApiException(String message) {
        super(message);
    }
    public abstract HttpStatus httpStatus();

    abstract ApiError apiError();

    abstract String userResponseMessage();

    public ApiErrorResponse createErrorResponse() {
        return new ApiErrorResponse(
                apiError(),
                userResponseMessage()
        );
    }
}


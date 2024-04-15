package com.ecommerce.wishlist.exception;

import org.springframework.http.HttpStatus;

public class MaxItemsException extends ApiException {

    public MaxItemsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    @Override
    ApiError apiError() {
        return ApiError.MAX_ITEMS;
    }

    @Override
    String userResponseMessage() {
        return getMessage();
    }
}

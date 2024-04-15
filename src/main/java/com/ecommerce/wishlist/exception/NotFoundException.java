package com.ecommerce.wishlist.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    ApiError apiError() {
        return ApiError.NOT_FOUND;
    }

    @Override
    String userResponseMessage() {
        return getMessage();
    }
}

package com.ecommerce.wishlist.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException {
    public AlreadyExistsException(String message) {
        super(message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    ApiError apiError() {
        return ApiError.WISHLIST_ALREADY_EXISTS;
    }

    @Override
    String userResponseMessage() {
        return getMessage();
    }
}

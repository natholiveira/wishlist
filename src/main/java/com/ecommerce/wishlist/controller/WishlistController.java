package com.ecommerce.wishlist.controller;

import com.ecommerce.wishlist.exception.AlreadyExistsException;
import com.ecommerce.wishlist.exception.MaxItemsException;
import com.ecommerce.wishlist.exception.NotFoundException;
import com.ecommerce.wishlist.response.ProductResponse;
import com.ecommerce.wishlist.response.WishlistResponse;
import com.ecommerce.wishlist.resquest.ProductRequest;
import com.ecommerce.wishlist.resquest.WishlistRequest;
import com.ecommerce.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Wishlist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wishlist created"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "422", description = "Maximum number of items in the wishlist"),
    })
    public WishlistResponse createWishlist(@Valid @RequestBody WishlistRequest wishlistRequest) throws MaxItemsException, AlreadyExistsException {
        return wishlistService.create(wishlistRequest);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get the user's wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get wishlist with success"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public WishlistResponse getWishlistByUserId(@PathVariable String userId) throws NotFoundException {
        return wishlistService.getByUserId(userId);
    }

    @GetMapping("/{userId}/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Checks if the item is in the list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item is on wishlist"),
            @ApiResponse(responseCode = "404", description = "Item not found in wishlist")
    })
    public ProductResponse isProductInWishlist(@PathVariable String userId, @PathVariable String productId) throws NotFoundException {
        return wishlistService.isProductInWishlist(userId, productId);
    }

    @DeleteMapping("/{userId}/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove product to wishlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully remove user's wish list"),
            @ApiResponse(responseCode = "404", description = "User or item not found")
    })
    public void removeProductFromWishlist(@PathVariable String userId, @PathVariable String productId) throws NotFoundException {
        wishlistService.removeProduct(userId, productId);
    }

    @PostMapping("/{userId}/products")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add product to wish list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully add user's wish list"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Maximum number of items in the wishlist"),
    })
    public WishlistResponse addProductToWishlist(@PathVariable String userId, @Valid @RequestBody ProductRequest productRequest) throws MaxItemsException, NotFoundException {
        return wishlistService.addProduct(userId, productRequest);
    }
}

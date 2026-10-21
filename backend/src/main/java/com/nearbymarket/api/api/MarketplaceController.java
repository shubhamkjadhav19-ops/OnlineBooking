package com.nearbymarket.api.api;

import com.nearbymarket.api.api.ApiModels.AccountResponse;
import com.nearbymarket.api.api.ApiModels.CartItemRequest;
import com.nearbymarket.api.api.ApiModels.CartResponse;
import com.nearbymarket.api.api.ApiModels.LocationRequest;
import com.nearbymarket.api.api.ApiModels.LoginRequest;
import com.nearbymarket.api.api.ApiModels.OrderDecisionRequest;
import com.nearbymarket.api.api.ApiModels.OrderResponse;
import com.nearbymarket.api.api.ApiModels.ProductRequest;
import com.nearbymarket.api.api.ApiModels.ProductResponse;
import com.nearbymarket.api.api.ApiModels.QuantityRequest;
import com.nearbymarket.api.api.ApiModels.RegisterRequest;
import com.nearbymarket.api.service.MarketplaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MarketplaceController {
	private final MarketplaceService marketplace;

	public MarketplaceController(MarketplaceService marketplace) {
		this.marketplace = marketplace;
	}

	@PostMapping("/auth/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse register(@Valid @RequestBody RegisterRequest request) {
		return marketplace.register(request);
	}

	@PostMapping("/auth/login")
	public AccountResponse login(@Valid @RequestBody LoginRequest request) {
		return marketplace.login(request.email(), request.password());
	}

	@PutMapping("/account/location")
	public AccountResponse updateLocation(Authentication authentication, @Valid @RequestBody LocationRequest request) {
		return marketplace.updateLocation(authentication.getName(), request);
	}

	@GetMapping("/products/search")
	public List<ProductResponse> searchProducts(Authentication authentication,
			@RequestParam(required = false) String query,
			@RequestParam(defaultValue = "10") double radiusKm) {
		return marketplace.searchProducts(authentication.getName(), query, radiusKm);
	}

	@GetMapping("/supplier/products")
	public List<ProductResponse> supplierProducts(Authentication authentication) {
		return marketplace.supplierProducts(authentication.getName());
	}

	@PostMapping("/supplier/products")
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse createProduct(Authentication authentication, @Valid @RequestBody ProductRequest request) {
		return marketplace.createProduct(authentication.getName(), request);
	}

	@PutMapping("/supplier/products/{productId}")
	public ProductResponse updateProduct(Authentication authentication, @PathVariable Long productId,
			@Valid @RequestBody ProductRequest request) {
		return marketplace.updateProduct(authentication.getName(), productId, request);
	}

	@DeleteMapping("/supplier/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProduct(Authentication authentication, @PathVariable Long productId) {
		marketplace.deleteProduct(authentication.getName(), productId);
	}

	@GetMapping("/customer/cart")
	public CartResponse getCart(Authentication authentication) {
		return marketplace.getCart(authentication.getName());
	}

	@PostMapping("/customer/cart/items")
	public CartResponse addToCart(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
		return marketplace.addToCart(authentication.getName(), request);
	}

	@PutMapping("/customer/cart/items/{itemId}")
	public CartResponse setCartQuantity(Authentication authentication, @PathVariable Long itemId,
			@Valid @RequestBody QuantityRequest request) {
		return marketplace.setCartQuantity(authentication.getName(), itemId, request.quantity());
	}

	@DeleteMapping("/customer/cart/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeCartItem(Authentication authentication, @PathVariable Long itemId) {
		marketplace.removeCartItem(authentication.getName(), itemId);
	}

	@PostMapping("/customer/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public List<OrderResponse> placeOrders(Authentication authentication) {
		return marketplace.placeOrders(authentication.getName());
	}

	@GetMapping("/customer/orders")
	public List<OrderResponse> customerOrders(Authentication authentication) {
		return marketplace.customerOrders(authentication.getName());
	}

	@GetMapping("/customer/orders/{orderId}")
	public OrderResponse customerOrder(Authentication authentication, @PathVariable Long orderId) {
		return marketplace.customerOrder(authentication.getName(), orderId);
	}

	@GetMapping("/supplier/orders")
	public List<OrderResponse> supplierOrders(Authentication authentication) {
		return marketplace.supplierOrders(authentication.getName());
	}

	@GetMapping("/supplier/orders/{orderId}")
	public OrderResponse supplierOrder(Authentication authentication, @PathVariable Long orderId) {
		return marketplace.supplierOrder(authentication.getName(), orderId);
	}

	@PatchMapping("/supplier/orders/{orderId}/decision")
	public OrderResponse decideOrder(Authentication authentication, @PathVariable Long orderId,
			@Valid @RequestBody OrderDecisionRequest request) {
		return marketplace.decideOrder(authentication.getName(), orderId, request.decision());
	}
}
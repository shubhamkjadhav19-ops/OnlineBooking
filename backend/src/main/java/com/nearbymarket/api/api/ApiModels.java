package com.nearbymarket.api.api;

import com.nearbymarket.api.model.OrderStatus;
import com.nearbymarket.api.model.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiModels {
	private ApiModels() {}

	public record RegisterRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Email @Size(max = 190) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotNull Role role,
		Double latitude,
		Double longitude) {}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
	public record AccountResponse(Long id, String name, String email, Role role, Double latitude, Double longitude) {}
	public record LocationRequest(@NotNull Double latitude, @NotNull Double longitude) {}

	public record ProductRequest(
		@NotBlank @Size(max = 140) String name,
		@Size(max = 1000) String description,
		@Size(max = 80) String category,
		@NotNull @DecimalMin(value = "0.01") BigDecimal price,
		@Min(0) int inventory) {}

	public record ProductResponse(
		Long id, String name, String description, String category, BigDecimal price, int inventory,
		Long supplierId, String supplierName, Double supplierLatitude, Double supplierLongitude, Double distanceKm) {}

	public record CartItemRequest(@NotNull Long productId, @Min(1) int quantity) {}
	public record QuantityRequest(@Min(1) int quantity) {}
	public record CartItemResponse(Long id, ProductResponse product, int quantity, BigDecimal subtotal) {}
	public record CartResponse(List<CartItemResponse> items, BigDecimal total) {}
	public record OrderDecisionRequest(@NotBlank String decision) {}
	public record OrderLineResponse(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}
	public record OrderResponse(
		Long id, OrderStatus status, Instant createdAt, Instant updatedAt,
		Long customerId, String customerName, Long supplierId, String supplierName,
		List<OrderLineResponse> items, BigDecimal total) {}
}
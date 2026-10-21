package com.nearbymarket.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nearbymarket.api.api.ApiModels.CartItemRequest;
import com.nearbymarket.api.api.ApiModels.LocationRequest;
import com.nearbymarket.api.api.ApiModels.OrderResponse;
import com.nearbymarket.api.api.ApiModels.ProductRequest;
import com.nearbymarket.api.api.ApiModels.ProductResponse;
import com.nearbymarket.api.api.ApiModels.RegisterRequest;
import com.nearbymarket.api.model.OrderStatus;
import com.nearbymarket.api.model.Role;
import com.nearbymarket.api.service.MarketplaceService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:marketplace-test;DB_CLOSE_DELAY=-1",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class MarketplaceApiApplicationTests {
	@Autowired
	private MarketplaceService marketplace;

	@Test
	void nearbyCustomerCanOrderAndTrackSupplierDecision() {
		String suffix = UUID.randomUUID().toString();
		String supplierEmail = "supplier-" + suffix + "@example.test";
		String customerEmail = "customer-" + suffix + "@example.test";
		marketplace.register(new RegisterRequest("Local Supplier", supplierEmail, "supplier-pass-123",
			Role.SUPPLIER, 40.7128, -74.0060));
		marketplace.register(new RegisterRequest("Nearby Customer", customerEmail, "customer-pass-123",
			Role.CUSTOMER, 40.7130, -74.0061));
		ProductResponse product = marketplace.createProduct(supplierEmail,
			new ProductRequest("Fresh Apples", "Local produce", "Fruit", new BigDecimal("2.50"), 5));

		assertEquals(1, marketplace.searchProducts(customerEmail, "apple", 10).size());
		marketplace.updateLocation(customerEmail, new LocationRequest(41.0, -74.0061));
		assertTrue(marketplace.searchProducts(customerEmail, "apple", 10).isEmpty());
		marketplace.updateLocation(customerEmail, new LocationRequest(40.7130, -74.0061));
		marketplace.addToCart(customerEmail, new CartItemRequest(product.id(), 2));
		OrderResponse placedOrder = marketplace.placeOrders(customerEmail).getFirst();
		assertEquals(OrderStatus.PENDING, placedOrder.status());
		assertEquals(new BigDecimal("5.00"), placedOrder.total());

		OrderResponse rejectedOrder = marketplace.decideOrder(supplierEmail, placedOrder.id(), "REJECT");
		assertEquals(OrderStatus.REJECTED, rejectedOrder.status());
		assertEquals(OrderStatus.REJECTED, marketplace.customerOrder(customerEmail, placedOrder.id()).status());
		assertEquals(5, marketplace.supplierProducts(supplierEmail).getFirst().inventory());
		assertFalse(marketplace.customerOrders(customerEmail).isEmpty());
		assertTrue(marketplace.login(customerEmail, "customer-pass-123").email().equals(customerEmail));
	}

}

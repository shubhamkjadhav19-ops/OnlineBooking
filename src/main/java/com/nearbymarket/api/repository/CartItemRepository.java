package com.nearbymarket.api.repository;

import com.nearbymarket.api.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findByCustomerId(Long customerId);
	Optional<CartItem> findByIdAndCustomerId(Long id, Long customerId);
	Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);
	void deleteByCustomerId(Long customerId);
}
package com.nearbymarket.api.repository;

import com.nearbymarket.api.model.MarketplaceOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, Long> {
	List<MarketplaceOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
	List<MarketplaceOrder> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
	Optional<MarketplaceOrder> findByIdAndCustomerId(Long id, Long customerId);
	Optional<MarketplaceOrder> findByIdAndSupplierId(Long id, Long supplierId);
}
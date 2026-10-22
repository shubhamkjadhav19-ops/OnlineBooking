package com.nearbymarket.api.repository;

import com.nearbymarket.api.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByActiveTrueAndInventoryGreaterThan(int inventory);
	List<Product> findBySupplierIdAndActiveTrue(Long supplierId);
	Optional<Product> findByIdAndSupplierId(Long id, Long supplierId);
}
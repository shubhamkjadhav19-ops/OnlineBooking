package com.nearbymarket.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
public class OrderLine {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private MarketplaceOrder order;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false, length = 140)
	private String productName;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false)
	private int quantity;

	protected OrderLine() {}

	public OrderLine(Long productId, String productName, BigDecimal unitPrice, int quantity) {
		this.productId = productId;
		this.productName = productName;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
	}

	void setOrder(MarketplaceOrder order) { this.order = order; }
	public Long getProductId() { return productId; }
	public String getProductName() { return productName; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public int getQuantity() { return quantity; }
}
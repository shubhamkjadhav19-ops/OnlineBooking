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
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 140)
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(length = 80)
	private String category;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private int inventory;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "supplier_id", nullable = false)
	private UserAccount supplier;

	protected Product() {}

	public Product(String name, String description, String category, BigDecimal price, int inventory, UserAccount supplier) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.price = price;
		this.inventory = inventory;
		this.supplier = supplier;
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getCategory() { return category; }
	public BigDecimal getPrice() { return price; }
	public int getInventory() { return inventory; }
	public boolean isActive() { return active; }
	public UserAccount getSupplier() { return supplier; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setCategory(String category) { this.category = category; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public void setInventory(int inventory) { this.inventory = inventory; }
	public void setActive(boolean active) { this.active = active; }
}
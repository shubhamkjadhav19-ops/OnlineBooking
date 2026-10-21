package com.nearbymarket.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 190)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	private Double latitude;
	private Double longitude;

	protected UserAccount() {}

	public UserAccount(String name, String email, String passwordHash, Role role, Double latitude, Double longitude) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public Role getRole() { return role; }
	public Double getLatitude() { return latitude; }
	public Double getLongitude() { return longitude; }
	public void setLatitude(Double latitude) { this.latitude = latitude; }
	public void setLongitude(Double longitude) { this.longitude = longitude; }
}
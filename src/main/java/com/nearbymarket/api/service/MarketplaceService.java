package com.nearbymarket.api.service;

import com.nearbymarket.api.api.ApiModels.AccountResponse;
import com.nearbymarket.api.api.ApiModels.CartItemResponse;
import com.nearbymarket.api.api.ApiModels.CartResponse;
import com.nearbymarket.api.api.ApiModels.LocationRequest;
import com.nearbymarket.api.api.ApiModels.OrderLineResponse;
import com.nearbymarket.api.api.ApiModels.OrderResponse;
import com.nearbymarket.api.api.ApiModels.ProductRequest;
import com.nearbymarket.api.api.ApiModels.ProductResponse;
import com.nearbymarket.api.api.ApiModels.RegisterRequest;
import com.nearbymarket.api.api.ApiModels.CartItemRequest;
import com.nearbymarket.api.model.CartItem;
import com.nearbymarket.api.model.MarketplaceOrder;
import com.nearbymarket.api.model.OrderLine;
import com.nearbymarket.api.model.OrderStatus;
import com.nearbymarket.api.model.Product;
import com.nearbymarket.api.model.Role;
import com.nearbymarket.api.model.UserAccount;
import com.nearbymarket.api.repository.CartItemRepository;
import com.nearbymarket.api.repository.MarketplaceOrderRepository;
import com.nearbymarket.api.repository.ProductRepository;
import com.nearbymarket.api.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MarketplaceService {
	private static final double DEFAULT_RADIUS_KM = 10.0;
	private final UserAccountRepository users;
	private final ProductRepository products;
	private final CartItemRepository cartItems;
	private final MarketplaceOrderRepository orders;
	private final PasswordEncoder passwordEncoder;

	public MarketplaceService(UserAccountRepository users, ProductRepository products,
			CartItemRepository cartItems, MarketplaceOrderRepository orders, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.products = products;
		this.cartItems = cartItems;
		this.orders = orders;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AccountResponse register(RegisterRequest request) {
		if (users.existsByEmailIgnoreCase(request.email())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}
		validateCoordinates(request.latitude(), request.longitude());
		UserAccount account = users.save(new UserAccount(request.name().trim(), request.email().trim().toLowerCase(),
			passwordEncoder.encode(request.password()), request.role(), request.latitude(), request.longitude()));
		return accountResponse(account);
	}

	@Transactional(readOnly = true)
	public AccountResponse login(String email, String password) {
		UserAccount account = users.findByEmailIgnoreCase(email)
			.filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
		return accountResponse(account);
	}

	@Transactional
	public AccountResponse updateLocation(String email, LocationRequest request) {
		validateCoordinates(request.latitude(), request.longitude());
		UserAccount account = currentUser(email);
		account.setLatitude(request.latitude());
		account.setLongitude(request.longitude());
		return accountResponse(account);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> searchProducts(String email, String query, double radiusKm) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		if (customer.getLatitude() == null || customer.getLongitude() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set your current location before searching");
		}
		double latitude = customer.getLatitude();
		double longitude = customer.getLongitude();
		if (radiusKm <= 0 || radiusKm > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radiusKm must be greater than 0 and no more than 100");
		}
		String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
		return products.findByActiveTrueAndInventoryGreaterThan(0).stream()
			.filter(product -> normalizedQuery.isEmpty()
				|| product.getName().toLowerCase().contains(normalizedQuery)
				|| (product.getCategory() != null && product.getCategory().toLowerCase().contains(normalizedQuery))
				|| (product.getDescription() != null && product.getDescription().toLowerCase().contains(normalizedQuery)))
			.map(product -> productResponse(product, latitude, longitude))
			.filter(result -> result.distanceKm() != null && result.distanceKm() <= radiusKm)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> supplierProducts(String email) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		return products.findBySupplierIdAndActiveTrue(supplier.getId()).stream()
			.map(product -> productResponse(product, null, null)).toList();
	}

	@Transactional
	public ProductResponse createProduct(String email, ProductRequest request) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		Product product = products.save(new Product(request.name().trim(), request.description(), request.category(),
			request.price(), request.inventory(), supplier));
		return productResponse(product, null, null);
	}

	@Transactional
	public ProductResponse updateProduct(String email, Long productId, ProductRequest request) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		Product product = ownedProduct(productId, supplier.getId());
		product.setName(request.name().trim());
		product.setDescription(request.description());
		product.setCategory(request.category());
		product.setPrice(request.price());
		product.setInventory(request.inventory());
		product.setActive(true);
		return productResponse(product, null, null);
	}

	@Transactional
	public void deleteProduct(String email, Long productId) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		ownedProduct(productId, supplier.getId()).setActive(false);
	}

	@Transactional(readOnly = true)
	public CartResponse getCart(String email) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		return cartResponse(cartItems.findByCustomerId(customer.getId()));
	}

	@Transactional
	public CartResponse addToCart(String email, CartItemRequest request) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		Product product = products.findById(request.productId())
			.filter(Product::isActive)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
		CartItem item = cartItems.findByCustomerIdAndProductId(customer.getId(), product.getId())
			.orElseGet(() -> new CartItem(customer, product, 0));
		int quantity = item.getQuantity() + request.quantity();
		if (quantity > product.getInventory()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested quantity exceeds available inventory");
		}
		item.setQuantity(quantity);
		cartItems.save(item);
		return cartResponse(cartItems.findByCustomerId(customer.getId()));
	}

	@Transactional
	public CartResponse setCartQuantity(String email, Long itemId, int quantity) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		CartItem item = cartItems.findByIdAndCustomerId(itemId, customer.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
		if (quantity > item.getProduct().getInventory()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested quantity exceeds available inventory");
		}
		item.setQuantity(quantity);
		return cartResponse(cartItems.findByCustomerId(customer.getId()));
	}

	@Transactional
	public void removeCartItem(String email, Long itemId) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		CartItem item = cartItems.findByIdAndCustomerId(itemId, customer.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
		cartItems.delete(item);
	}

	@Transactional
	public List<OrderResponse> placeOrders(String email) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		List<CartItem> items = cartItems.findByCustomerId(customer.getId());
		if (items.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
		}
		for (CartItem item : items) {
			if (!item.getProduct().isActive() || item.getQuantity() > item.getProduct().getInventory()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "A cart product is unavailable or out of stock");
			}
		}
		Map<Long, List<CartItem>> bySupplier = items.stream()
			.collect(Collectors.groupingBy(item -> item.getProduct().getSupplier().getId()));
		List<MarketplaceOrder> created = new ArrayList<>();
		for (List<CartItem> supplierItems : bySupplier.values()) {
			UserAccount supplier = supplierItems.getFirst().getProduct().getSupplier();
			MarketplaceOrder order = new MarketplaceOrder(customer, supplier);
			for (CartItem item : supplierItems) {
				Product product = item.getProduct();
				product.setInventory(product.getInventory() - item.getQuantity());
				order.addItem(new OrderLine(product.getId(), product.getName(), product.getPrice(), item.getQuantity()));
			}
			created.add(orders.save(order));
		}
		cartItems.deleteByCustomerId(customer.getId());
		return created.stream().map(this::orderResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> customerOrders(String email) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		return orders.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream().map(this::orderResponse).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse customerOrder(String email, Long orderId) {
		UserAccount customer = requireRole(email, Role.CUSTOMER);
		return orders.findByIdAndCustomerId(orderId, customer.getId()).map(this::orderResponse)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> supplierOrders(String email) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		return orders.findBySupplierIdOrderByCreatedAtDesc(supplier.getId()).stream().map(this::orderResponse).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse supplierOrder(String email, Long orderId) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		return orders.findByIdAndSupplierId(orderId, supplier.getId()).map(this::orderResponse)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	@Transactional
	public OrderResponse decideOrder(String email, Long orderId, String decision) {
		UserAccount supplier = requireRole(email, Role.SUPPLIER);
		MarketplaceOrder order = orders.findByIdAndSupplierId(orderId, supplier.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending orders can be decided");
		}
		if ("ACCEPT".equalsIgnoreCase(decision)) {
			order.setStatus(OrderStatus.ACCEPTED);
		} else if ("REJECT".equalsIgnoreCase(decision)) {
			order.setStatus(OrderStatus.REJECTED);
			for (OrderLine line : order.getItems()) {
				products.findById(line.getProductId()).ifPresent(product -> product.setInventory(product.getInventory() + line.getQuantity()));
			}
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "decision must be ACCEPT or REJECT");
		}
		return orderResponse(order);
	}

	private UserAccount currentUser(String email) {
		return users.findByEmailIgnoreCase(email)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
	}

	private UserAccount requireRole(String email, Role role) {
		UserAccount account = currentUser(email);
		if (account.getRole() != role) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action requires the " + role + " role");
		}
		return account;
	}

	private Product ownedProduct(Long productId, Long supplierId) {
		return products.findByIdAndSupplierId(productId, supplierId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
	}

	private AccountResponse accountResponse(UserAccount account) {
		return new AccountResponse(account.getId(), account.getName(), account.getEmail(), account.getRole(),
			account.getLatitude(), account.getLongitude());
	}

	private ProductResponse productResponse(Product product, Double latitude, Double longitude) {
		UserAccount supplier = product.getSupplier();
		Double distance = latitude == null || supplier.getLatitude() == null || supplier.getLongitude() == null
			? null : distanceKm(latitude, longitude, supplier.getLatitude(), supplier.getLongitude());
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getCategory(),
			product.getPrice(), product.getInventory(), supplier.getId(), supplier.getName(), supplier.getLatitude(),
			supplier.getLongitude(), distance);
	}

	private CartResponse cartResponse(List<CartItem> items) {
		List<CartItemResponse> responseItems = items.stream().map(item -> new CartItemResponse(item.getId(),
			productResponse(item.getProduct(), null, null), item.getQuantity(),
			item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).toList();
		BigDecimal total = responseItems.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new CartResponse(responseItems, total);
	}

	private OrderResponse orderResponse(MarketplaceOrder order) {
		List<OrderLineResponse> lineResponses = order.getItems().stream().map(line -> new OrderLineResponse(
			line.getProductId(), line.getProductName(), line.getUnitPrice(), line.getQuantity(),
			line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))).toList();
		BigDecimal total = lineResponses.stream().map(OrderLineResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new OrderResponse(order.getId(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt(),
			order.getCustomer().getId(), order.getCustomer().getName(), order.getSupplier().getId(),
			order.getSupplier().getName(), lineResponses, total);
	}

	private static void validateCoordinates(Double latitude, Double longitude) {
		if (latitude != null && (latitude < -90 || latitude > 90)
				|| longitude != null && (longitude < -180 || longitude > 180)
				|| (latitude == null) != (longitude == null)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide valid latitude and longitude together");
		}
	}

	private static double distanceKm(double latitude1, double longitude1, double latitude2, double longitude2) {
		double latitudeDelta = Math.toRadians(latitude2 - latitude1);
		double longitudeDelta = Math.toRadians(longitude2 - longitude1);
		double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
			+ Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
			* Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
		return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}
}
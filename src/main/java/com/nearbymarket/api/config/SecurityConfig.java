package com.nearbymarket.api.config;

import com.nearbymarket.api.repository.UserAccountRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UserAccountRepository users) {
		return email -> users.findByEmailIgnoreCase(email)
			.map(account -> User.withUsername(account.getEmail())
				.password(account.getPasswordHash())
				.roles(account.getRole().name())
				.build())
			.orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(email));
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}") List<String> allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(requests -> requests
				.requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
				.anyRequest().authenticated())
			.httpBasic(Customizer.withDefaults())
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
			.build();
	}
}

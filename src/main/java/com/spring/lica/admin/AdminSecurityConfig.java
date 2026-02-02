package com.spring.lica.admin;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(1)
@EnableConfigurationProperties(AdminProperties.class)
public class AdminSecurityConfig {

	@Bean
	public SecurityFilterChain adminFilterChain(HttpSecurity http, AdminProperties props) throws Exception {
		http
			.securityMatcher("/admin/**", "/login", "/css/**")
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/login", "/css/**").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
			)
			.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/admin/settings", true)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/admin/logout")
				.logoutSuccessUrl("/login?logout")
			);
		return http.build();
	}

	@Bean
	public InMemoryUserDetailsManager adminUserDetailsService(AdminProperties props, PasswordEncoder passwordEncoder) {
		UserDetails admin = User.builder()
			.username(props.getUsername())
			.password(passwordEncoder.encode(props.getPassword()))
			.roles("ADMIN")
			.build();
		return new InMemoryUserDetailsManager(admin);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

package com.event.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.event.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	UserDetailsServiceImpl detailsServiceImpl() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
		authenticationProvider.setUserDetailsService(detailsServiceImpl());
		return authenticationProvider;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.csrf(csrf -> csrf.disable())
		.authorizeHttpRequests(request -> request.requestMatchers("/","/index","/login","/register","/forgot-password","/forgotPassword","/otp-verification","/resendotp","/verify-otp","/change-password","/update-password").permitAll().anyRequest().authenticated())
		.formLogin(formlogin -> formlogin.loginPage("/login").defaultSuccessUrl("/userHome",true)
				 .failureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error=true")))
		.logout(logout -> logout.permitAll());
		
		return http.build();
	}
	
}

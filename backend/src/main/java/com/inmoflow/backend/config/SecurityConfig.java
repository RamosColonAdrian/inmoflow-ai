package com.inmoflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/agencies", "/api/agencies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agencies", "/api/agencies/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/properties", "/api/properties/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();
    }
}
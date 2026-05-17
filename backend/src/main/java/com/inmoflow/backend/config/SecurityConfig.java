package com.inmoflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/agencies", "/api/agencies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agencies", "/api/agencies/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/properties", "/api/properties/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/leads", "/api/leads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/leads", "/api/leads/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/conversations", "/api/conversations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/conversations", "/api/conversations/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/ai/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
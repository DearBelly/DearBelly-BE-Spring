package com.hanium.mom4u.global.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.response.ErrorResponse;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.JwtAuthenticationFilter;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/test/**","/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/uploads/**",
                                "/actuator/**",
                                "/api/v1/scan")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/v1/auth/logout").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/auth/withdraw").hasRole("USER")
                        .requestMatchers("/api/v1/schedules/**").hasRole("USER")
                        .requestMatchers("/api/v1/family-code/**").hasRole("USER")
                        .requestMatchers("/api/v1/member/**").hasRole("USER")
                        .requestMatchers("/api/v1/baby/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/news/*/bookmark").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/news/*/bookmark").hasRole("USER")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/news/bookmarks").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        // 401: 인증 실패(토큰 없음/만료/무효)
                        .authenticationEntryPoint((request, response, authEx) -> {
                            // 필터에서 남겨준 코드가 있으면 사용
                            StatusCode code = (StatusCode) request.getAttribute("auth_error_status");
                            if (code == null) code = StatusCode.INVALID_JWT_TOKEN;
                            writeError(response, code);
                        })
                        // 403: 권한 부족
                        .accessDeniedHandler((request, response, accessDeniedEx) -> {
                            writeError(response, StatusCode.UNAUTHORIZED_ACCESS);
                        })
                )
                .build();
    }

    private void writeError(HttpServletResponse response, StatusCode status) throws IOException {
        var body = ErrorResponse.builder()
                .httpStatus(status.getHttpStatus())
                .code(status.getCode())
                .message(status.getDescription())
                .build();
        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://dearbelly.site"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


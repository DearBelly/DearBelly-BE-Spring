package com.hanium.mom4u.global.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.response.ErrorResponse;
import com.hanium.mom4u.global.response.StatusCode;
import com.hanium.mom4u.global.security.jwt.JwtAuthenticationFilter;
import com.hanium.mom4u.global.security.jwt.JwtTokenProvider;
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
                        .requestMatchers("/test/**","/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/uploads/**",
                                "/api/v1/auth/**",
                                "/actuator/**",
                                "/api/v1/scan")
                        .permitAll()
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
                        // 토큰 없음/무효 → 회원전용입니다
                        .authenticationEntryPoint((request, response, authEx) -> {
                            var body = ErrorResponse.builder()
                                    .httpStatus(StatusCode.MEMBER_ONLY.getHttpStatus())
                                    .code(StatusCode.MEMBER_ONLY.getCode())
                                    .message(StatusCode.MEMBER_ONLY.getDescription())   // "회원을 조회할 수 없습니다."
                                    .build();

                            response.setStatus(StatusCode.MEMBER_ONLY.getHttpStatus().value()); // 404
                            response.setContentType("application/json;charset=UTF-8");
                            new ObjectMapper().writeValue(response.getWriter(), body);
                        })
                        // 인증은 됐지만 권한 부족 → 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            var body = ErrorResponse.builder()
                                    .httpStatus(StatusCode.UNAUTHORIZED_ACCESS.getHttpStatus())
                                    .code(StatusCode.UNAUTHORIZED_ACCESS.getCode())
                                    .message(StatusCode.UNAUTHORIZED_ACCESS.getDescription())
                                    .build();

                            response.setStatus(StatusCode.UNAUTHORIZED_ACCESS.getHttpStatus().value());
                            response.setContentType("application/json;charset=UTF-8");
                            new ObjectMapper().writeValue(response.getWriter(), body);
                        })
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://dearbelly.site"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


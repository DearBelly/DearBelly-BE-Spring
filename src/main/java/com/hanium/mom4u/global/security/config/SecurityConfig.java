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
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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

    private final JwtTokenProvider jwtTokenProvider;

    // 공개(메서드 무관)
    private static final String[] PUBLIC_ANY = {
            "/test/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
            "/uploads/**", "/actuator/**", "/api/v1/scan"
    };
    // 공개 GET
    private static final String[] PUBLIC_GET = {
            "/api/v1/letters/theme",
            "/api/v1/news/**"
    };
    // 공개 PUT
    private static final String[] PUBLIC_PUT = {
            "/api/v1/letters/theme"
    };

    // 인증 필요(ROLE_USER 이상) – 메서드 무관
    private static final String[] USER_ANY = {
            "/api/v1/schedules/**", "/api/v1/family-code/**",
            "/api/v1/member/**", "/api/v1/baby/**", "/api/v1/letters/**"
    };
    private static final String[] USER_DELETE = {
            "/api/v1/auth/withdraw", "/api/v1/news/*/bookmark"
    };
    private static final String[] USER_PUT = {
            "/api/v1/news/*/bookmark"
    };

    // 로그인/회원가입 등 진짜 공개해야 하는 auth 엔드포인트만 지정
    private static final String[] AUTH_PUBLIC_GET = {
            "/api/v1/auth/url/**",   // 구글/카카오 URL 조회
            "/api/v1/auth/naver"     // 네이버 URL 조회
    };
    private static final String[] AUTH_PUBLIC_POST = {
            "/api/v1/auth/google",   // 코드 교환
            "/api/v1/auth/kakao",
            "/api/v1/auth/naver",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout"
    };

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        var rh = new RoleHierarchyImpl();
        rh.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return rh;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    //    USER 전용 (메서드 무관/별도)
                    for (String p : USER_ANY)    auth.requestMatchers(p).hasRole("USER");
                    for (String p : USER_PUT)    auth.requestMatchers(HttpMethod.PUT,    p).hasRole("USER");
                    for (String p : USER_DELETE) auth.requestMatchers(HttpMethod.DELETE, p).hasRole("USER");

                    // 2) 공개 auth (공개해야 하는 것)
                    for (String p : AUTH_PUBLIC_GET)  auth.requestMatchers(HttpMethod.GET,  p).permitAll();
                    for (String p : AUTH_PUBLIC_POST) auth.requestMatchers(HttpMethod.POST, p).permitAll();

                    // 3) 공개 그룹 (메서드 무관/별도)
                    for (String p : PUBLIC_ANY)  auth.requestMatchers(p).permitAll();
                    for (String p : PUBLIC_GET)  auth.requestMatchers(HttpMethod.GET,  p).permitAll();
                    for (String p : PUBLIC_PUT)  auth.requestMatchers(HttpMethod.PUT,  p).permitAll();

                    // 4) 나머지는 인증 필요
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            // 401
                            StatusCode code = (StatusCode) req.getAttribute("auth_error_status");
                            if (code == null) code = StatusCode.INVALID_JWT_TOKEN;
                            writeError(res, code);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            // 403
                            writeError(res, StatusCode.UNAUTHORIZED_ACCESS);
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
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}



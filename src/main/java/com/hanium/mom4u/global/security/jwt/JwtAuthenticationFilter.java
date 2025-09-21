package com.hanium.mom4u.global.security.jwt;

import com.hanium.mom4u.global.exception.GeneralException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;


    private static final List<RequestMatcher> PUBLIC_MATCHERS = List.of(
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-resources/**"),
            new AntPathRequestMatcher("/test/**"),
            new AntPathRequestMatcher("/actuator/**"),
            new AntPathRequestMatcher("/api/v1/scan"),

            new AntPathRequestMatcher("/api/v1/auth/url/**", "GET"),
            new AntPathRequestMatcher("/api/v1/auth/naver",  "GET"),

            new AntPathRequestMatcher("/api/v1/auth/google", "POST"),
            new AntPathRequestMatcher("/api/v1/auth/kakao",  "POST"),
            new AntPathRequestMatcher("/api/v1/auth/naver",  "POST"),

            new AntPathRequestMatcher("/api/v1/auth/refresh", "POST"),
            new AntPathRequestMatcher("/api/v1/auth/logout",  "POST")
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        for (RequestMatcher m : PUBLIC_MATCHERS) {
            if (m.matches(request)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = jwtTokenProvider.resolveToken(req);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT OK: {} {} as {}", req.getMethod(), req.getRequestURI(), auth.getName());
            } else {
                SecurityContextHolder.clearContext();
                log.debug("JWT missing/invalid: {} {}", req.getMethod(), req.getRequestURI());
            }

            chain.doFilter(req, res);

        } catch (GeneralException ex) {
            // 401로 처리되게 힌트 남기고 컨텍스트 비움
            SecurityContextHolder.clearContext();
            log.warn("JWT validation failed: {} {} code={} msg={}",
                    req.getMethod(), req.getRequestURI(), ex.getStatusCode().name(), ex.getMessage());
            req.setAttribute("auth_error_status", ex.getStatusCode());
            chain.doFilter(req, res); // 응답은 EntryPoint가 작성

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.error("JWT filter unexpected error: {} {}", req.getMethod(), req.getRequestURI(), ex);
            throw ex; // 전역 예외 처리기로 전달
        }
    }
}

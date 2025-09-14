package com.hanium.mom4u.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.ErrorResponse;
import com.hanium.mom4u.global.response.StatusCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        boolean shouldNotFilter = path.startsWith("/api/v1/auth") ||
                path.startsWith("/swagger-ui/")||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources/")||
                path.startsWith("/test") ||
                path.startsWith("/actuator") ||
                path.startsWith("/api/v1/scan");

        return shouldNotFilter;
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
             //401로 처리되게 힌트 남기고 컨텍스트 비움
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



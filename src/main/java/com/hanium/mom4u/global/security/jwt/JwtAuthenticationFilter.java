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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String ANON_KEY = "dearbelly-anon-key";

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

        String token = jwtTokenProvider.resolveToken(req);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                Authentication anonymous = new AnonymousAuthenticationToken(
                        ANON_KEY, "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                );
                SecurityContextHolder.getContext().setAuthentication(anonymous);
            }

            chain.doFilter(req, res);

        } catch (Exception e) {
            throw e;
        }
    }
}



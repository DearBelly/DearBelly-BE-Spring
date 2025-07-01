package com.hanium.mom4u.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("Request path: " + path);

        boolean shouldNotFilter = path.startsWith("/api/v1/auth") ||
                path.startsWith("/swagger-ui/")||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources/")||
                path.startsWith("/error");
        System.out.println("Should not filter: " + shouldNotFilter);

        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (GeneralException ex) {
            setErrorResponse(response, ex.getStatusCode());
        } catch (Exception e) {
            setErrorResponse(response, StatusCode.TOKEN_NOT_FOUND);
        }

    }

    private void setErrorResponse(HttpServletResponse response, StatusCode statusCode) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .httpStatus(statusCode.getHttpStatus())
                .code(statusCode.getCode())
                .message(statusCode.getDescription())
                .build();

        response.setStatus(statusCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}

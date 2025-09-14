package com.hanium.mom4u.global.security.jwt;

import com.hanium.mom4u.domain.member.common.Role;
import com.hanium.mom4u.domain.member.entity.Member;
import com.hanium.mom4u.domain.member.repository.MemberRepository;
import com.hanium.mom4u.global.exception.GeneralException;
import com.hanium.mom4u.global.response.StatusCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";

    @Value("${spring.jwt.secret}")
    private String secretKey;

    private Key key;

    @Value("${spring.jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    @Value("${spring.jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;

    private final MemberRepository memberRepository;

    public JwtTokenProvider(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createAccessToken(Long memberId, Role role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim(ROLES_CLAIM, List.of(role.name()))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInSeconds * 1000);
        return Jwts.builder()
                .setSubject(memberId.toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String memberId = claims.getSubject();

        // 토큰의 roles 클레임 사용
        List<String> roles = claims.get(ROLES_CLAIM, List.class);

        // 예전 토큰 등 roles가 없으면 DB fallback (정합성↑)
        if (roles == null || roles.isEmpty()) {
            Member member = memberRepository.findById(Long.valueOf(memberId))
                    .orElseThrow(() -> GeneralException.of(StatusCode.MEMBER_NOT_FOUND));
            roles = List.of(member.getRole().name());
        }

        var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsernamePasswordAuthenticationToken(memberId, null, authorities);
    }

    public String getMemberId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw GeneralException.of(StatusCode.INVALID_JWT_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw GeneralException.of(StatusCode.INVALID_JWT_TOKEN);
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7)
                : null;
    }
}

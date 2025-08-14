package kr.elta.backend.configuration;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Duration accessExp,
            @Value("${jwt.refresh-expiration}") Duration refreshExp
    ) {
        this.key                  = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs   = accessExp.toMillis();
        this.refreshExpirationMs  = refreshExp.toMillis();
    }

    // 액세스 토큰 생성
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // 서명+만료 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 만료 여부만 확인
    public boolean isTokenExpired(String token) {
        try {
            Date exp = parseClaims(token).getBody().getExpiration();
            return exp.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).getBody().get("role", String.class);
    }
}
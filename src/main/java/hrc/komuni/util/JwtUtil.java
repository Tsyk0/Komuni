package hrc.komuni.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret-key:TSKEY}")
    private String rawSecretKey;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    // 非静态变量，用于注入
    private String secretKey;
    private long expiration;

    @PostConstruct
    public void init() {
        System.out.println("=== JWT配置初始化 ===");
        System.out.println("原始密钥: " + rawSecretKey);
        System.out.println("原始密钥长度: " + rawSecretKey.length());

        // 处理密钥：确保长度足够
        String key = rawSecretKey;
        if (key.length() < 64) {
            StringBuilder sb = new StringBuilder(key);
            while (sb.length() < 64) {
                sb.append("0");
            }
            key = sb.toString();
        }
        this.secretKey = key.substring(0, Math.min(key.length(), 64));
        this.expiration = expirationTime;

        System.out.println("处理后密钥长度: " + this.secretKey.length());
        System.out.println("Token过期时间(ms): " + this.expiration);
        System.out.println("==================");
    }

    // 改为非静态方法
    public String generateToken(long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .compact();
    }

    // 改为非静态方法
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    // 其他方法也改为非静态
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = getUsernameFromToken(token);
            return (username.equals(extractedUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            String userIdStr = getUsernameFromToken(token);
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }
}
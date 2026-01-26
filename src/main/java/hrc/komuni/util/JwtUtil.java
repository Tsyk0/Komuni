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

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @return JWT Token字符串
     */
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

    /**
     * 解析Token获取Claims
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中获取用户ID字符串（Subject）
     * @param token JWT Token
     * @return 用户ID字符串
     */
    public String getUserIdStringFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从Token中获取用户ID（Long类型）
     * @param token JWT Token
     * @return 用户ID，解析失败返回null
     */
    public Long getUserIdFromToken(String token) {
        try {
            String userIdStr = getUserIdStringFromToken(token);
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查Token是否过期
     * @param token JWT Token
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // 解析异常视为过期
        }
    }

    /**
     * 验证Token有效性
     * @param token JWT Token
     * @param userId 用户ID
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token, Long userId) {
        try {
            Long tokenUserId = getUserIdFromToken(token);
            return tokenUserId != null &&
                    tokenUserId.equals(userId) &&
                    !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 快速验证Token（不检查具体用户，只验证签名和过期）
     * 适用于WebSocket连接等场景
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean validateTokenQuick(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查过期时间
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return false;
            }
            // 检查是否有用户ID
            String userIdStr = claims.getSubject();
            return userIdStr != null && !userIdStr.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token过期剩余时间（秒）
     * @param token JWT Token
     * @return 剩余秒数，-1表示无效或已过期
     */
    public long getTokenRemainingSeconds(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            if (expiration.before(now)) {
                return -1; // 已过期
            }

            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            return -1;
        }
    }
}
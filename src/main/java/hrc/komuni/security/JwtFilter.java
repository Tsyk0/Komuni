package hrc.komuni.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Component  // 添加这个！
public class JwtFilter extends OncePerRequestFilter {

    @Autowired  // 注入JwtUtil，统一使用
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            try {
                // 去掉 Bearer 前缀
                token = token.substring(7);

                // 使用 JwtUtil 统一解析（而不是自己解析）
                Claims claims = jwtUtil.parseToken(token);

                // 检查是否过期
                if (claims.getExpiration().before(new Date())) {
                    throw new RuntimeException("Token 已过期");
                }

                // 将用户ID保存到请求属性
                request.setAttribute("userId", Long.parseLong(claims.getSubject()));

            } catch (RuntimeException e) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "无效或过期的 Token: " + e.getMessage());
                return;
            }
        } else {
            // 对于需要认证的接口但没有token的情况
            String requestURI = request.getRequestURI();
            if (requiresAuthentication(requestURI)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "需要认证 Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.error(status, message);

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiResponse));
        writer.flush();
    }

    private boolean requiresAuthentication(String uri) {
        System.out.println("   🧐 检查路径是否需要认证: " + uri);

        // 这些是公开路径（不需要认证）
        String[] publicPaths = {
                "/user/loginCheck",
                "/user/insertUser",
                "/user/selectUserByUserId",
                "/user/debugInjection",
                "/user/testJwt",
                "/user/tokenUsageDemo",
                "/user/testTokenExpiration",
                "/user/checkToken",
        };

        for (String path : publicPaths) {
            if (uri.equals(path)) {
                System.out.println("   ✅ 是公开路径: " + path);
                return false;
            }
        }

        // 所有其他 /user/ 开头的路径都需要认证
        if (uri.startsWith("/user/")) {
            System.out.println("   🔒 需要认证: " + uri);
            return true;
        }

        // 其他模块...
        return uri.startsWith("/friend/")
                || uri.startsWith("/conv/")
                || uri.startsWith("/message/");
    }
}
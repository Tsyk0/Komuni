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

@Component // 添加这个！
public class JwtFilter extends OncePerRequestFilter {

    @Autowired // 注入JwtUtil，统一使用
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

    /**
     * 检查请求路径是否需要身份认证
     *
     * @param uri 请求路径，如 "/user/login"、"/friend/list"
     * @return true表示需要认证，false表示公开路径
     */
    private boolean requiresAuthentication(String uri) {
        // 打印调试信息
        System.out.println("   🧐 检查路径是否需要认证: " + uri);

        // ==================== 1. 公开路径白名单 ====================
        // 这些路径完全不需要认证（完全匹配）
        String[] publicPaths = {
                "/user/loginCheck",
                "/user/insertUser",
                "/user/checkToken",
                "/websocket/testConnection",
                "/websocket/stats",
        };

        // 检查是否在公开路径白名单中
        for (String publicPath : publicPaths) {
            if (uri.equals(publicPath)) {
                System.out.println("   ✅ 是公开路径（白名单）: " + publicPath);
                return false; // 不需要认证
            }
        }

        // ==================== 2. 需要认证的模块 ====================
        // 定义所有需要认证的模块前缀
        String[] securedModules = {
                "/user/", // 用户模块（除白名单外）
                "/friendRelationDetail/",
                "/conversationDetail/",
                "/messageDetail/",
                "/message/"
        };

        // 检查是否属于需要认证的模块
        for (String module : securedModules) {
            if (uri.startsWith(module)) {
                System.out.println("   🔒 需要认证（" + module + "模块）: " + uri);
                return true; // 需要认证
            }
        }

        // ==================== 3. 默认处理 ====================
        // 不属于任何模块的路径（如静态资源、健康检查等）
        System.out.println("   ✅ 公开路径（默认）: " + uri);
        return false; // 默认不需要认证
    }
}
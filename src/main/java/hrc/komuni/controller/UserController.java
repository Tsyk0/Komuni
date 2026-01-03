package hrc.komuni.controller;

import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.UserService;
import hrc.komuni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从Authorization头中提取纯Token（去掉Bearer前缀）
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }

        String token = authHeader.trim();

        // 去除Bearer前缀（如果存在）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        // 检查Token是否为空
        if (token.isEmpty()) {
            return null;
        }

        return token;
    }

    /**
     * 验证Token是否有效，返回Claims对象
     */
    private Claims validateAndParseToken(String authHeader) throws Exception {
        String token = extractTokenFromHeader(authHeader);
        if (token == null) {
            throw new IllegalArgumentException("Authorization头为空或无效");
        }

        return jwtUtil.parseToken(token);
    }

    @GetMapping("/debugInjection")
    public ApiResponse<Map<String, Object>> debugInjection() {
        Map<String, Object> data = new HashMap<>();

        data.put("userServiceInjected", userService != null);
        data.put("jwtUtilInjected", jwtUtil != null);

        if (jwtUtil != null) {
            try {
                String testToken = jwtUtil.generateToken(999L);
                data.put("jwtUtilWorks", true);
                data.put("testToken", testToken);

                // 测试Token信息
                Claims claims = jwtUtil.parseToken(testToken);
                data.put("testTokenUserId", claims.getSubject());
                data.put("testTokenExpiration", claims.getExpiration());

            } catch (Exception e) {
                data.put("jwtUtilWorks", false);
                data.put("jwtUtilError", e.getMessage());
                data.put("errorType", e.getClass().getName());
            }
        }

        return ApiResponse.success("注入调试", data);
    }

    @GetMapping("/selectUserByUserId")
    public ApiResponse<User> selectUserByUserId(@RequestParam Long userId) {
        try {
            User user = userService.selectUserByUserId(userId);
            if (user == null) {
                return ApiResponse.notFound("用户不存在");
            }
            return ApiResponse.success("查询成功", user);
        } catch (Exception e) {
            return ApiResponse.serverError("查询用户失败: " + e.getMessage());
        }
    }

    @PostMapping("/insertUser")
    public ApiResponse<Long> insertUser(@RequestBody User user) {
        try {
            Long userId = userService.insertUser(user);
            if (userId == 0) {
                return ApiResponse.badRequest("注册失败：用户名或密码不能为空");
            }
            return ApiResponse.success("注册成功", userId);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Duplicate entry")) {
                return ApiResponse.badRequest("注册失败：手机号或邮箱已存在");
            }
            return ApiResponse.serverError("注册失败: " + errorMsg);
        }
    }

    @PostMapping(value = "/loginCheck", consumes = "application/json")
    public ApiResponse<Map<String, Object>> loginCheck(@RequestBody Map<String, String> loginRequest) {
        try {

            Long userId = Long.parseLong(loginRequest.get("userId"));
            String userPwd = loginRequest.get("userPwd");

            if (userService.selectUserByUserId(userId) == null) {
                return ApiResponse.badRequest("不存在的账户");
            }

            if (userService.checkUserPwd(userId, userPwd)) {
                // 生成纯Token（不包含Bearer）
                String token = jwtUtil.generateToken(userId);

                User user = userService.selectUserByUserId(userId);

                // 更新在线状态
                userService.updateOnlineStatus(userId, 1);

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);  // 只返回纯Token
                data.put("userId", userId);
                data.put("user", user);

                // 添加Token详细信息（用于调试）
                Claims claims = jwtUtil.parseToken(token);
                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("issuedAt", claims.getIssuedAt());
                tokenInfo.put("expiration", claims.getExpiration());
                tokenInfo.put("expiresInSeconds", (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
                data.put("tokenInfo", tokenInfo);

                System.out.println("=== 用户登录成功 ===");
                System.out.println("用户ID: " + userId);
                System.out.println("Token: " + token.substring(0, Math.min(30, token.length())) + "...");
                System.out.println("过期时间: " + claims.getExpiration());

                return ApiResponse.success("登录成功", data);
            } else {
                return ApiResponse.unauthorized("账号或密码错误");
            }
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("用户ID格式错误");
        } catch (Exception e) {
            return ApiResponse.serverError("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateUserPwdByUserId")
    public ApiResponse<String> updateUserPwdByUserId(
            @RequestParam Long userId,
            @RequestParam String newPwd) {
        try {
            String result = userService.updateUserPwdByUserId(userId, newPwd);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.serverError("修改密码失败: " + e.getMessage());
        }
    }

    @GetMapping("/checkToken")
    public ApiResponse<Map<String, Object>> checkToken(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("\n\n==========================================");
            System.out.println("🚀 checkToken 接口被调用");
            System.out.println("==========================================");
            System.out.println("📥 收到的 Authorization 头原始值:");
            System.out.println("   \"" + authHeader + "\"");
            // 使用统一方法提取和验证Token
            Claims claims = validateAndParseToken(authHeader);

            System.out.println("\n\n==========================================");
            System.out.println("🚀 checkToken 接口被调用");
            System.out.println("==========================================");
            System.out.println("📥 收到的 Authorization 头原始值:");
            System.out.println("   \"" + authHeader + "\"");

            Date expiration = claims.getExpiration();

            if (expiration.before(new Date())) {
                return ApiResponse.unauthorized("Token 已过期");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("userId", claims.getSubject());
            data.put("expiration", expiration);
            data.put("issuedAt", claims.getIssuedAt());
            data.put("valid", true);
            data.put("remainingSeconds", (expiration.getTime() - System.currentTimeMillis()) / 1000);

            return ApiResponse.success("Token 有效", data);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 处理过期Token的特殊情况
            Map<String, Object> expiredData = new HashMap<>();
            expiredData.put("valid", false);
            expiredData.put("userId", e.getClaims().getSubject());
            expiredData.put("originalExpiration", e.getClaims().getExpiration());
            expiredData.put("expiredSecondsAgo", (System.currentTimeMillis() - e.getClaims().getExpiration().getTime()) / 1000);

            return ApiResponse.success("Token 已过期", expiredData);
        } catch (io.jsonwebtoken.SignatureException e) {
            return ApiResponse.badRequest("Token 签名无效");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            return ApiResponse.badRequest("Token 格式错误");
        } catch (IllegalArgumentException e) {
            return ApiResponse.unauthorized("需要认证 Token: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.serverError("Token 校验失败: " + e.getMessage());
        }
    }

    @GetMapping("/getUserIdByToken")
    public ApiResponse<Long> getUserIdByToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // 使用统一方法提取和验证Token
            Claims claims = validateAndParseToken(authHeader);

            Long userId = Long.parseLong(claims.getSubject());
            return ApiResponse.success(userId);
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("Token中的用户ID格式错误");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ApiResponse.unauthorized("Token 已过期");
        } catch (IllegalArgumentException e) {
            return ApiResponse.unauthorized("需要认证 Token");
        } catch (Exception e) {
            return ApiResponse.unauthorized("无效的Token: " + e.getMessage());
        }
    }

    @PostMapping("/updateUserAllAttriByUserId")
    public ApiResponse<String> updateUserAllAttriByUserId(@RequestBody User user) {
        try {
            String result = userService.updateUserAllAttriByUserId(user);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.serverError("更新用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectUsersCondition")
    public ApiResponse<List<User>> selectUsersCondition(
            @RequestParam String key,
            @RequestParam Integer amount) {
        try {
            List<User> users = userService.selectUsersCondition(key, amount);
            return ApiResponse.success("查询成功", users);
        } catch (Exception e) {
            return ApiResponse.serverError("搜索用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/testJwt")
    public ApiResponse<Map<String, Object>> testJwt() {
        try {
            Long testUserId = 123L;
            String token = jwtUtil.generateToken(testUserId);
            Claims claims = jwtUtil.parseToken(token);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenWithBearer", "Bearer " + token);  // 示例：如何添加Bearer前缀
            data.put("userId", claims.getSubject());
            data.put("expiresAt", claims.getExpiration());
            data.put("isValid", jwtUtil.validateToken(token, String.valueOf(testUserId)));

            return ApiResponse.success("JWT测试成功", data);
        } catch (Exception e) {
            return ApiResponse.serverError("JWT测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/refreshToken")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ApiResponse.unauthorized("需要提供Token");
            }

            // 尝试解析Token（即使过期也可以解析）
            Claims claims;
            try {
                claims = jwtUtil.parseToken(token);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // 如果Token过期，从异常中获取claims
                claims = e.getClaims();
            }

            Long userId = Long.parseLong(claims.getSubject());

            // 生成新Token
            String newToken = jwtUtil.generateToken(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);  // 只返回纯Token
            data.put("userId", userId);

            // 添加新Token的信息
            Claims newClaims = jwtUtil.parseToken(newToken);
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("issuedAt", newClaims.getIssuedAt());
            tokenInfo.put("expiration", newClaims.getExpiration());
            data.put("tokenInfo", tokenInfo);

            return ApiResponse.success("Token刷新成功", data);

        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("用户ID格式错误");
        } catch (Exception e) {
            return ApiResponse.unauthorized("刷新Token失败: " + e.getMessage());
        }
    }


}
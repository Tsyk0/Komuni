package hrc.komuni.controller;

import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.UserService;
import hrc.komuni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等操作接口")
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

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

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
    @Operation(summary = "依赖注入调试", description = "调试用户服务和JWT工具的依赖注入状态")
    public ApiResponse<Map<String, Object>> debugInjection() {
        Map<String, Object> data = new HashMap<>();

        data.put("userServiceInjected", userService != null);
        data.put("jwtUtilInjected", jwtUtil != null);

        if (jwtUtil != null) {
            try {
                String testToken = jwtUtil.generateToken(999L);
                data.put("jwtUtilWorks", true);
                data.put("testToken", testToken);

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
    @Operation(summary = "查询用户信息", description = "根据用户ID查询用户的详细信息")
    public ApiResponse<User> selectUserByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
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
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ApiResponse<Long> insertUser(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
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
    @Operation(summary = "用户登录", description = "用户登录验证，成功返回JWT Token")
    public ApiResponse<Map<String, Object>> loginCheck(
            @Parameter(description = "登录请求参数", required = true) @RequestBody Map<String, String> loginRequest) {
        try {

            Long userId = Long.parseLong(loginRequest.get("userId"));
            String userPwd = loginRequest.get("userPwd");

            if (userService.selectUserByUserId(userId) == null) {
                return ApiResponse.badRequest("不存在的账户");
            }

            if (userService.checkUserPwd(userId, userPwd)) {
                String token = jwtUtil.generateToken(userId);

                User user = userService.selectUserByUserId(userId);

                userService.updateOnlineStatus(userId, 1);

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", userId);
                data.put("user", user);

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
    @Operation(summary = "修改密码", description = "修改指定用户的登录密码")
    public ApiResponse<String> updateUserPwdByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "新密码", required = true) @RequestParam String newPwd) {
        try {
            String result = userService.updateUserPwdByUserId(userId, newPwd);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.serverError("修改密码失败: " + e.getMessage());
        }
    }

    @GetMapping("/checkToken")
    @Operation(summary = "验证Token有效性", description = "验证JWT Token是否有效和未过期")
    public ApiResponse<Map<String, Object>> checkToken(
            @Parameter(description = "Authorization头", required = true) @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("\n\n==========================================");
            System.out.println("🚀 checkToken 接口被调用");
            System.out.println("==========================================");
            System.out.println("📥 收到的 Authorization 头原始值:");
            System.out.println("   \"" + authHeader + "\"");

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
    @Operation(summary = "从Token获取用户ID", description = "从JWT Token中解析出用户ID")
    public ApiResponse<Long> getUserIdByToken(
            @Parameter(description = "Authorization头", required = true) @RequestHeader("Authorization") String authHeader) {
        try {
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
    @Operation(summary = "更新用户信息", description = "更新用户的全部个人信息")
    public ApiResponse<String> updateUserAllAttriByUserId(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        try {
            String result = userService.updateUserAllAttriByUserId(user);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.serverError("更新用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectUsersCondition")
    @Operation(summary = "条件搜索用户", description = "根据关键词搜索用户（支持昵称和ID模糊搜索）")
    public ApiResponse<List<User>> selectUsersCondition(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String key,
            @Parameter(description = "返回数量限制", required = true) @RequestParam Integer amount) {
        try {
            List<User> users = userService.selectUsersCondition(key, amount);
            return ApiResponse.success("查询成功", users);
        } catch (Exception e) {
            return ApiResponse.serverError("搜索用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/testJwt")
    @Operation(summary = "JWT功能测试", description = "测试JWT Token生成和验证功能")
    public ApiResponse<Map<String, Object>> testJwt() {
        try {
            Long testUserId = 123L;
            String token = jwtUtil.generateToken(testUserId);
            Claims claims = jwtUtil.parseToken(token);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenWithBearer", "Bearer " + token);
            data.put("userId", claims.getSubject());
            data.put("expiresAt", claims.getExpiration());
            data.put("isValid", jwtUtil.validateToken(token, String.valueOf(testUserId)));

            return ApiResponse.success("JWT测试成功", data);
        } catch (Exception e) {
            return ApiResponse.serverError("JWT测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "刷新Token", description = "使用旧的Token刷新生成新的有效Token")
    public ApiResponse<Map<String, Object>> refreshToken(
            @Parameter(description = "Authorization头", required = true) @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ApiResponse.unauthorized("需要提供Token");
            }

            Claims claims;
            try {
                claims = jwtUtil.parseToken(token);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                claims = e.getClaims();
            }

            Long userId = Long.parseLong(claims.getSubject());

            String newToken = jwtUtil.generateToken(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            data.put("userId", userId);

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
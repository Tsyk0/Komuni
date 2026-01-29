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

import java.util.HashMap;
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

    @PostMapping("/insertUser")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ApiResponse<Long> insertUser(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        System.out.println("=== 用户注册 ===");
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
                System.out.println("=== 用户登录失败:acc error ===");
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
                System.out.println("=== 用户登录失败:pwd error ===");
                return ApiResponse.unauthorized("账号或密码错误");
            }
        } catch (NumberFormatException e) {
            return ApiResponse.badRequest("用户ID格式错误");
        } catch (Exception e) {
            return ApiResponse.serverError("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/checkToken")
    @Operation(summary = "验证Token有效性", description = "验证JWT Token并返回完整用户信息")
    public ApiResponse<Map<String, Object>> checkToken(
            @Parameter(description = "Authorization头", required = true) @RequestHeader("Authorization") String authHeader) {

        // 提取 token
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            // 解析 token
            Claims claims = jwtUtil.parseToken(token);

            // 获取用户ID
            String subject = claims.getSubject();
            Long userId = Long.parseLong(subject);

            // 查询用户信息
            User user = userService.selectUserByUserId(userId);

            if (user == null) {
                return ApiResponse.error("用户不存在"); // 不带 data 参数
            }

            // 准备响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", userId);
            data.put("user", user);

            // Token 信息
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            tokenInfo.put("expiresInSeconds", (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
            data.put("tokenInfo", tokenInfo);

            // 验证状态
            data.put("valid", true);
            data.put("remainingSeconds", (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);

            return ApiResponse.success("Token 有效", data);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token 过期的情况 - 仍然返回成功格式，但 valid 为 false
            Map<String, Object> expiredData = new HashMap<>();
            expiredData.put("valid", false);
            expiredData.put("userId", e.getClaims().getSubject());
            expiredData.put("originalExpiration", e.getClaims().getExpiration());
            expiredData.put("expiredSecondsAgo", (System.currentTimeMillis() - e.getClaims().getExpiration().getTime()) / 1000);

            return ApiResponse.success("Token 已过期", expiredData);

        } catch (NumberFormatException e) {
            // 用户ID格式错误
            return ApiResponse.error("用户ID格式错误");

        } catch (io.jsonwebtoken.SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            // Token 签名或格式错误
            return ApiResponse.error("Token无效");

        } catch (Exception e) {
            // 其他错误
            return ApiResponse.error("Token验证失败: " + e.getMessage());
        }
    }
}
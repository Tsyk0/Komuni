package hrc.komuni.controller;

import hrc.komuni.util.ImageBase64Util;
import org.springframework.beans.factory.annotation.Autowired;
import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.UserService;
import hrc.komuni.util.ImageBase64Util;
import hrc.komuni.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等操作接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Autowired
    private ImageBase64Util imageBase64Util;

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
    @Operation(summary = "用户登录", description = "使用账号密码登录，成功返回 JWT 与用户信息并写入 Cookie。会话校验请使用 GET /user/checkToken")
    public ApiResponse<Map<String, Object>> loginCheck(
            @Parameter(description = "登录请求参数", required = true) @RequestBody Map<String, String> loginRequest,
            HttpServletResponse response) {
        try {
            Long userId = Long.parseLong(loginRequest.get("userId"));
            String userPwd = loginRequest.get("userPwd");
            boolean rememberMe = Boolean.parseBoolean(loginRequest.getOrDefault("rememberMe", "false"));

            if (userService.selectUserByUserId(userId) == null) {
                System.out.println("=== 用户登录失败:acc error ===");
                return ApiResponse.badRequest("不存在的账户");
            }

            if (userService.checkUserPwd(userId, userPwd)) {
                String token = jwtUtil.generateToken(userId);

                User user = userService.selectUserByUserId(userId);

                userService.updateOnlineStatus(userId, 1);

                Claims claims = jwtUtil.parseToken(token);

                // 先清除旧的 token cookie（同名、Path 一致），避免浏览器不覆盖导致仍用旧用户 token
                ResponseCookie clearCookie = ResponseCookie.from("token", "")
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .path("/")
                        .sameSite(cookieSameSite)
                        .maxAge(0)
                        .build();
                response.addHeader("Set-Cookie", clearCookie.toString());

                ResponseCookie cookie = ResponseCookie.from("token", token)
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .path("/")
                        .sameSite(cookieSameSite)
                        .maxAge(rememberMe ? 7L * 24 * 60 * 60 : -1)
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userId", userId);
                data.put("user", user);

                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("issuedAt", claims.getIssuedAt());
                tokenInfo.put("expiration", claims.getExpiration());
                tokenInfo.put("expiresInSeconds",
                        (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        String token = null;
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        }
        if (token == null || token.trim().isEmpty()) {
            token = getTokenFromCookies(request);
        }

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
            expiredData.put("expiredSecondsAgo",
                    (System.currentTimeMillis() - e.getClaims().getExpiration().getTime()) / 1000);

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

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (javax.servlet.http.Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @GetMapping("/selectUserByUserId")
    @Operation(summary = "查询用户信息", description = "根据用户ID查询用户的详细信息")
    public ApiResponse<User> selectUserByUserId(
            @Parameter(description = "用户ID", required = true) @RequestAttribute Long userId) {
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

    @PostMapping("/updateUserPwdByOldPwd")
    @Operation(summary = "修改密码", description = "校验原密码后更新新密码")
    public ApiResponse<String> updateUserPwdByOldPwd(
            @RequestAttribute Long userId,
            @RequestBody Map<String, String> body) {
        try {
            String oldPwd = body.get("oldPwd");
            String newPwd = body.get("newPwd");

            if (oldPwd == null || oldPwd.trim().isEmpty() || newPwd == null || newPwd.trim().isEmpty()) {
                return ApiResponse.badRequest("原密码或新密码不能为空");
            }

            if (!userService.checkUserPwd(userId, oldPwd)) {
                return ApiResponse.unauthorized("原密码错误");
            }

            String result = userService.updateUserPwdByUserId(userId, newPwd);
            return "更新成功".equals(result)
                    ? ApiResponse.success(result)
                    : ApiResponse.badRequest(result);
        } catch (Exception e) {
            return ApiResponse.serverError("修改密码失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateUserAllAttriByUserId")
    @Operation(summary = "更新用户信息", description = "更新用户的全部个人信息")
    public ApiResponse<String> updateUserAllAttriByUserId(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        try {
            System.out.println("=== 收到用户更新请求 ===");
            System.out.println("用户ID: " + user.getUserId());
            System.out.println("昵称: " + user.getUserNickname());
            System.out.println("头像字段存在: " + (user.getUserAvatar() != null));

            if (user.getUserAvatar() != null) {
                System.out.println("头像数据前50字符: " +
                        user.getUserAvatar().substring(0, Math.min(50, user.getUserAvatar().length())));
            }

            // 1. 获取旧头像路径（用于删除）
            User oldUser = userService.selectUserByUserId(user.getUserId());
            String oldAvatarPath = null;
            if (oldUser != null) {
                oldAvatarPath = oldUser.getUserAvatar();
                System.out.println("旧头像路径: " + oldAvatarPath);
            }

            // 2. 处理头像（如果是base64格式）
            String userAvatar = user.getUserAvatar();
            if (userAvatar != null && !userAvatar.isEmpty()) {
                if (imageBase64Util.isBase64Image(userAvatar)) {
                    System.out.println("检测到base64图片，开始处理...");
                    // 保存base64图片为文件，获取相对路径
                    String newAvatarPath = imageBase64Util.saveBase64Image(userAvatar, user.getUserId());
                    user.setUserAvatar(newAvatarPath);
                    System.out.println("新头像路径: " + newAvatarPath);

                    // 删除旧头像文件
                    if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
                        imageBase64Util.deleteOldAvatar(oldAvatarPath);
                    }
                } else {
                    System.out.println("头像不是base64格式，可能是已有路径: " + userAvatar);
                }
            } else {
                System.out.println("未提供头像数据，保持原头像不变");
                // 保持原头像不变
                if (oldUser != null) {
                    user.setUserAvatar(oldUser.getUserAvatar());
                }
            }

            // 3. 更新用户信息
            String result = userService.updateUserAllAttriByUserId(user);
            System.out.println("更新结果: " + result);

            return ApiResponse.success(result);

        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.serverError("更新用户信息失败: " + e.getMessage());
        }
    }
}
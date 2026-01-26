package hrc.komuni.controller;

import hrc.komuni.manager.WebSocketSessionManager;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/session")
@Tag(name = "会话状态管理", description = "WebSocket会话状态查询和管理接口")
public class SessionController {
    @Autowired
    ConversationMemberService conversationMemberService;

    @GetMapping("/getUserAllSession")
    @Operation(summary = "查询用户的所有WebSocket会话", description = "查询用户在所有会话中的WebSocket连接状态")
    public ApiResponse<Map<String, Set<WebSocketSession>>> getUserAllSession(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            // 1. 获取用户的所有会话ID
            List<Long> convIds = conversationMemberService.selectConvIdsByUserId(userId);

            System.out.println("用户 " + userId + " 的会话 ID：" + convIds);

            Map<String, Set<WebSocketSession>> result = new HashMap<>();

            // 2. 查询每个会话的在线用户
            for (Long convId : convIds) {
                // 使用新的方法名
                Set<WebSocketSession> convSessions = WebSocketSessionManager.getConversationSubscribers(convId);
                result.put("convId:" + convId, convSessions);

                // 输出调试信息
                System.out.println("会话 " + convId + " 的在线用户数: " + convSessions.size());
                for (WebSocketSession session : convSessions) {
                    Long sessionUserId = WebSocketSessionManager.getUserIdByConnection(session);
                    System.out.println("  - 用户ID: " + sessionUserId + ", 会话ID: " + session.getId());
                }
            }

            // 3. 也返回用户的当前连接状态
            WebSocketSession userSession = WebSocketSessionManager.getUserConnection(userId);
            if (userSession != null) {
                result.put("currentUserConnection", Collections.singleton(userSession));
                System.out.println("用户 " + userId + " 当前连接状态: " +
                        (userSession.isOpen() ? "在线" : "离线"));
            }

            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            System.err.println("查询会话失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.serverError("查询会话失败: " + e.getMessage());
        }
    }

    /**
     * 新增：获取WebSocket连接统计信息
     */
    @GetMapping("/getWebSocketStats")
    @Operation(summary = "获取WebSocket连接统计信息")
    public ApiResponse<Map<String, Object>> getWebSocketStats() {
        try {
            Map<String, Object> stats = WebSocketSessionManager.getStatistics();
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.serverError("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 新增：获取用户在线状态
     */
    @GetMapping("/checkUserOnline")
    @Operation(summary = "检查用户是否在线")
    public ApiResponse<Boolean> checkUserOnline(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            WebSocketSession session = WebSocketSessionManager.getUserConnection(userId);
            boolean isOnline = session != null && session.isOpen();
            return ApiResponse.success("查询成功", isOnline);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }
}
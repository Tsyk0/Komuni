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
            List<hrc.komuni.entity.ConversationMember> members = conversationMemberService.selectByUserId(userId);

            List<Long> convIds = new ArrayList<>();
            for (hrc.komuni.entity.ConversationMember member : members) {
                convIds.add(member.getConvId());
            }

            System.out.println("用户的会话 ID：" + convIds);

            Map<String, Set<WebSocketSession>> result = new HashMap<>();

            for (Long convId : convIds) {
                Set<WebSocketSession> convSessions = WebSocketSessionManager.getConvSessions(convId);
                result.put("convId:" + convId, convSessions);
                System.out.println("会话 ID " + convId + " 的 WebSocket 会话：" + convSessions);
            }

            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话失败: " + e.getMessage());
        }
    }
}
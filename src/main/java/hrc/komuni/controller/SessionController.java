package hrc.komuni.controller;

import hrc.komuni.manager.WebSocketSessionManager;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    ConversationService conversationService;

    // 获取用户的所有WebSocket会话
    @GetMapping("/getUserAllSession")
    public ApiResponse<Map<String, Set<WebSocketSession>>> getUserAllSession(@RequestParam Long userId) {
        try {
            // 查询该用户参与的所有会话 ID
            List<Long> convIds = conversationService.selectConvIdsByUserId(userId);
            System.out.println("用户的会话 ID：" + convIds);

            // 创建结果集
            Map<String, Set<WebSocketSession>> result = new HashMap<>();

            // 遍历会话 ID，获取每个会话的 WebSocket 会话集合
            for (Long convId : convIds) {
                Set<WebSocketSession> convSessions = WebSocketSessionManager.getConvSessions(convId);
                // 将会话 ID 和对应的会话集合放入结果集
                result.put("convId:" + convId, convSessions);
                System.out.println("会话 ID " + convId + " 的 WebSocket 会话：" + convSessions);
            }

            // 返回所有会话数据
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话失败: " + e.getMessage());
        }
    }
}
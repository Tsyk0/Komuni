package hrc.komuni.controller;

import hrc.komuni.manager.WebSocketSessionManager;
import hrc.komuni.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/websocket")
@Tag(name = "WebSocket调试接口", description = "用于测试WebSocket连接的接口")
public class WebSocketDebugController {

    /**
     * 测试WebSocket连接状态
     */
    @GetMapping("/testConnection")
    @Operation(summary = "测试WebSocket连接")
    public ApiResponse<String> testWebSocket() {
        try {
            // 返回简单的连接信息
            String info = "WebSocket服务运行正常\n";
            info += "服务器时间: " + System.currentTimeMillis() + "\n";
            info += "WebSocket端点: ws://localhost:8080/ws?token={your_token}";

            return ApiResponse.success("WebSocket服务正常", info);
        } catch (Exception e) {
            return ApiResponse.serverError("WebSocket服务异常: " + e.getMessage());
        }
    }

    /**
     * 获取当前WebSocket统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取WebSocket统计信息")
    public ApiResponse<Object> getWebSocketStats() {
        try {
            return ApiResponse.success("获取成功", WebSocketSessionManager.getStatistics());
        } catch (Exception e) {
            return ApiResponse.serverError("获取失败: " + e.getMessage());
        }
    }
}
package hrc.komuni.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.manager.WebSocketSessionManager;
import hrc.komuni.service.*;
import hrc.komuni.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ConversationMemberService conversationMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageReadStatusService messageReadStatusService;

    // ========== 连接生命周期管理 ==========

    /**
     * WebSocket连接建立时调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("\n=== WebSocket连接建立 ===");
        System.out.println("会话ID: " + session.getId());
        System.out.println("远程地址: " + session.getRemoteAddress());

        try {
            // 1. 从URL参数获取token
            String token = extractTokenFromSession(session);
            if (token == null || token.isEmpty()) {
                closeWithError(session, "未提供认证token");
                return;
            }

            System.out.println("收到Token，长度: " + token.length());

            // 2. 验证token并获取用户ID
            Long userId;
            try {
                userId = validateWebSocketToken(token);
                if (userId == null) {
                    closeWithError(session, "Token验证失败：无法获取用户ID");
                    return;
                }
                System.out.println("Token验证成功，用户ID: " + userId);
            } catch (IllegalArgumentException e) {
                System.err.println("Token验证失败: " + e.getMessage());
                closeWithError(session, "Token验证失败: " + e.getMessage());
                return;
            } catch (Exception e) {
                System.err.println("Token验证异常: " + e.getMessage());
                e.printStackTrace();
                closeWithError(session, "认证服务异常");
                return;
            }

            // 3. 添加到连接管理器
            WebSocketSessionManager.addUserConnection(userId, session);

            // 4. 获取用户的所有会话并自动订阅
            List<Long> userConversations = conversationMemberService.selectConvIdsByUserId(userId);
            System.out.println("用户 " + userId + " 参与的会话数: " +
                    (userConversations != null ? userConversations.size() : 0));

            // 5. 订阅所有会话
            if (userConversations != null && !userConversations.isEmpty()) {
                int subscribedCount = 0;
                for (Long convId : userConversations) {
                    try {
                        WebSocketSessionManager.subscribeToConversation(convId, session);
                        subscribedCount++;
                    } catch (Exception e) {
                        System.err.println("订阅会话 " + convId + " 失败: " + e.getMessage());
                    }
                }
                System.out.println("成功订阅 " + subscribedCount + " 个会话");
            } else {
                System.out.println("⚠️ 用户 " + userId + " 当前没有参与任何会话");
            }

            // 6. 更新用户在线状态
            try {
                userService.updateOnlineStatus(userId, 1);
            } catch (Exception e) {
                System.err.println("更新在线状态失败: " + e.getMessage());
                // 继续执行，不要因为更新状态失败而中断连接
            }

            // 7. 发送连接成功响应
            sendConnectionSuccess(session, userId, userConversations);

            System.out.println("✅ 用户 " + userId + " WebSocket连接建立成功");
            System.out.println("========================\n");

        } catch (Exception e) {
            System.err.println("连接建立过程异常: " + e.getMessage());
            e.printStackTrace();
            closeWithError(session, "连接建立异常: " + e.getMessage());
        }
    }

    /**
     * WebSocket连接关闭时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("\n=== WebSocket连接关闭 ===");
        System.out.println("会话ID: " + session.getId());
        System.out.println("关闭码: " + status.getCode() + ", 原因: " + status.getReason());

        try {
            // 1. 从连接管理器移除
            WebSocketSessionManager.removeConnection(session);

            // 2. 获取用户ID并更新离线状态
            Long userId = WebSocketSessionManager.getUserIdByConnection(session);
            if (userId != null) {
                // 检查是否还有其他连接（为多设备预留）
                if (WebSocketSessionManager.getUserConnection(userId) == null) {
                    try {
                        userService.updateOnlineStatus(userId, 0);
                        System.out.println("👤 用户 " + userId + " 已离线");
                    } catch (Exception e) {
                        System.err.println("更新离线状态失败: " + e.getMessage());
                    }
                }
            }

            System.out.println("========================\n");

        } catch (Exception e) {
            System.err.println("连接关闭处理异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== 消息处理 ==========

    /**
     * 处理接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("\n📨 收到WebSocket消息，长度: " + payload.length());

        // 1. 心跳消息处理
        if ("ping".equals(payload)) {
            handleHeartbeat(session);
            return;
        }

        try {
            // 2. 解析JSON消息
            JSONObject messageJson = JSON.parseObject(payload);
            String action = messageJson.getString("action");

            if (action == null || action.isEmpty()) {
                sendErrorResponse(session, "消息格式错误: 缺少action字段");
                return;
            }

            // 3. 获取发送者用户ID
            Long userId = WebSocketSessionManager.getUserIdByConnection(session);
            if (userId == null) {
                sendErrorResponse(session, "无法识别发送者，连接未注册");
                return;
            }

            System.out.println("处理动作: " + action + ", 发送者: " + userId);

            // 4. 根据action类型分发处理
            switch (action) {
                case "sendMessage":
                    handleSendMessage(session, messageJson, userId);
                    break;

                case "readMessage":
                    handleReadMessage(session, messageJson, userId);
                    break;

                case "recallMessage":
                    handleRecallMessage(session, messageJson, userId);
                    break;

                case "typing":
                    handleTypingStatus(session, messageJson, userId);
                    break;

                case "subscribe":
                    handleSubscribeRequest(session, messageJson, userId);
                    break;

                case "unsubscribe":
                    handleUnsubscribeRequest(session, messageJson, userId);
                    break;

                default:
                    sendErrorResponse(session, "不支持的操作类型: " + action);
            }

        } catch (Exception e) {
            System.err.println("消息处理异常: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("\n❌ WebSocket传输错误:");
        System.err.println("会话ID: " + session.getId());
        System.err.println("错误信息: " + exception.getMessage());
        exception.printStackTrace();
    }

    // ========== 核心业务处理方法 ==========

    /**
     * 处理发送消息请求
     */
    private void handleSendMessage(WebSocketSession session, JSONObject messageJson, Long senderId) throws Exception {
        System.out.println("\n=== 处理发送消息请求 ===");

        // 1. 提取参数
        Long convId = messageJson.getLong("convId");
        String messageType = messageJson.getString("messageType");
        String messageContent = messageJson.getString("messageContent");
        Long replyToMessageId = messageJson.getLong("replyToMessageId");

        // 2. 参数验证
        if (convId == null) {
            sendErrorResponse(session, "缺少会话ID(convId)");
            return;
        }
        if (messageType == null || messageContent == null) {
            sendErrorResponse(session, "消息类型或内容不能为空");
            return;
        }

        System.out.println("发送者: " + senderId + ", 会话: " + convId + ", 类型: " + messageType);

        // 3. 验证用户是否在会话中
        if (!isUserInConversation(senderId, convId)) {
            sendErrorResponse(session, "您不在该会话中，无法发送消息");
            return;
        }

        // 4. 保存消息到数据库（调用你原有的messageService）
        hrc.komuni.entity.Message message = new hrc.komuni.entity.Message();
        message.setConvId(convId);
        message.setSenderId(senderId);
        message.setMessageType(messageType);
        message.setMessageContent(messageContent);
        message.setMessageStatus(0); // 发送中
        message.setIsRecalled(false);
        message.setReplyToMessageId(replyToMessageId);

        Long messageId = messageService.insertMessage(message);
        if (messageId == null) {
            sendErrorResponse(session, "消息保存失败");
            return;
        }

        System.out.println("✅ 消息保存成功，ID: " + messageId);

        // 5. 更新消息状态为已发送
        message.setMessageId(messageId);
        message.setMessageStatus(1); // 已发送
        messageService.updateMessageStatus(messageId, 1);

        // 6. 构建广播消息
        JSONObject broadcastMessage = new JSONObject();
        broadcastMessage.put("action", "newMessage");
        broadcastMessage.put("messageId", messageId);
        broadcastMessage.put("convId", convId);
        broadcastMessage.put("senderId", senderId);
        broadcastMessage.put("messageType", messageType);
        broadcastMessage.put("messageContent", messageContent);
        broadcastMessage.put("sendTime", System.currentTimeMillis());
        broadcastMessage.put("messageStatus", 1);
        if (replyToMessageId != null) {
            broadcastMessage.put("replyToMessageId", replyToMessageId);
        }

        // 7. 向会话的所有订阅者广播（排除发送者自己）
        broadcastToConversation(convId, broadcastMessage, senderId);

        // 8. 发送确认给发送者
        JSONObject ackMessage = new JSONObject();
        ackMessage.put("action", "messageSent");
        ackMessage.put("messageId", messageId);
        ackMessage.put("convId", convId);
        ackMessage.put("success", true);
        ackMessage.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(ackMessage.toJSONString()));

        System.out.println("✅ 消息发送完成，已广播给会话 " + convId + " 的其他成员");
        System.out.println("========================\n");
    }

    /**
     * 向会话广播消息
     */
    private void broadcastToConversation(Long convId, JSONObject message, Long excludeUserId) {
        Set<WebSocketSession> subscribers = WebSocketSessionManager.getConversationSubscribers(convId);

        if (subscribers == null || subscribers.isEmpty()) {
            System.out.println("⚠️ 会话 " + convId + " 当前没有在线订阅者");
            return;
        }

        System.out.println("📢 开始广播消息到会话 " + convId + "，订阅者数: " + subscribers.size());

        String messageStr = message.toJSONString();
        int deliveredCount = 0;

        for (WebSocketSession subscriber : subscribers) {
            try {
                // 排除发送者自己
                Long subscriberUserId = WebSocketSessionManager.getUserIdByConnection(subscriber);
                if (subscriberUserId != null && subscriberUserId.equals(excludeUserId)) {
                    continue;
                }

                // 检查连接是否有效
                if (subscriber.isOpen()) {
                    subscriber.sendMessage(new TextMessage(messageStr));
                    deliveredCount++;
                } else {
                    // 连接已关闭，清理
                    System.out.println("⚠️ 检测到关闭的连接，清理订阅");
                    WebSocketSessionManager.removeConnection(subscriber);
                }
            } catch (Exception e) {
                System.err.println("❌ 广播消息失败: " + e.getMessage());
            }
        }

        System.out.println("🎯 广播完成，成功发送: " + deliveredCount + "/" + subscribers.size());
    }

    /**
     * 处理消息已读请求
     */
    private void handleReadMessage(WebSocketSession session, JSONObject messageJson, Long userId) throws Exception {
        Long messageId = messageJson.getLong("messageId");
        Long convId = messageJson.getLong("convId");

        if (messageId == null || convId == null) {
            sendErrorResponse(session, "需要messageId和convId参数");
            return;
        }

        System.out.println("👀 用户 " + userId + " 标记消息 " + messageId + " 为已读");

        // 调用你原有的已读状态服务
        messageReadStatusService.markMessageAsRead(messageId, userId);

        // 更新最后阅读时间
        conversationMemberService.updateLastReadTime(convId, userId);

        // 发送确认响应
        JSONObject response = new JSONObject();
        response.put("action", "messageRead");
        response.put("messageId", messageId);
        response.put("convId", convId);
        response.put("userId", userId);
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(response.toJSONString()));
    }

    /**
     * 处理消息撤回请求
     */
    private void handleRecallMessage(WebSocketSession session, JSONObject messageJson, Long userId) throws Exception {
        Long messageId = messageJson.getLong("messageId");
        Long convId = messageJson.getLong("convId");

        if (messageId == null || convId == null) {
            sendErrorResponse(session, "需要messageId和convId参数");
            return;
        }

        System.out.println("↩️ 用户 " + userId + " 尝试撤回消息 " + messageId);

        // 调用你原有的撤回逻辑
        int result = messageService.recallMessage(messageId, userId);
        if (result > 0) {
            // 构建撤回通知
            JSONObject recallNotification = new JSONObject();
            recallNotification.put("action", "messageRecalled");
            recallNotification.put("messageId", messageId);
            recallNotification.put("convId", convId);
            recallNotification.put("userId", userId);
            recallNotification.put("timestamp", System.currentTimeMillis());

            // 广播撤回通知
            broadcastToConversation(convId, recallNotification, null);

            // 发送确认给发起者
            JSONObject response = new JSONObject();
            response.put("action", "messageRecallSuccess");
            response.put("messageId", messageId);
            response.put("success", true);

            session.sendMessage(new TextMessage(response.toJSONString()));

            System.out.println("✅ 消息撤回成功");
        } else {
            sendErrorResponse(session, "消息撤回失败");
        }
    }

    /**
     * 处理用户输入状态
     */
    private void handleTypingStatus(WebSocketSession session, JSONObject messageJson, Long userId) throws Exception {
        Long convId = messageJson.getLong("convId");
        Boolean isTyping = messageJson.getBoolean("isTyping");

        if (convId == null || isTyping == null) {
            sendErrorResponse(session, "需要convId和isTyping参数");
            return;
        }

        // 构建输入状态消息
        JSONObject typingMessage = new JSONObject();
        typingMessage.put("action", "userTyping");
        typingMessage.put("convId", convId);
        typingMessage.put("userId", userId);
        typingMessage.put("isTyping", isTyping);
        typingMessage.put("timestamp", System.currentTimeMillis());

        // 广播给会话其他成员（排除自己）
        broadcastToConversation(convId, typingMessage, userId);

        System.out.println("⌨️  用户 " + userId + " 输入状态: " + (isTyping ? "正在输入..." : "停止输入"));
    }

    // ========== 订阅管理方法 ==========

    /**
     * 处理订阅请求（动态加入会话）
     */
    private void handleSubscribeRequest(WebSocketSession session, JSONObject messageJson, Long userId) throws Exception {
        Long convId = messageJson.getLong("convId");

        if (convId == null) {
            sendErrorResponse(session, "需要提供会话ID(convId)");
            return;
        }

        System.out.println("📌 用户 " + userId + " 请求订阅会话 " + convId);

        // 验证用户是否在会话中
        if (!isUserInConversation(userId, convId)) {
            sendErrorResponse(session, "您不在该会话中，无法订阅");
            return;
        }

        // 订阅会话（开始接收消息）
        WebSocketSessionManager.subscribeToConversation(convId, session);

        // 发送成功响应
        JSONObject response = new JSONObject();
        response.put("action", "subscribed");
        response.put("convId", convId);
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(response.toJSONString()));

        System.out.println("✅ 用户 " + userId + " 成功订阅会话 " + convId);
    }

    /**
     * 处理取消订阅请求
     */
    private void handleUnsubscribeRequest(WebSocketSession session, JSONObject messageJson, Long userId) throws Exception {
        Long convId = messageJson.getLong("convId");

        if (convId == null) {
            sendErrorResponse(session, "需要提供会话ID(convId)");
            return;
        }

        System.out.println("📌 用户 " + userId + " 请求取消订阅会话 " + convId);

        // 取消订阅会话（停止接收消息）
        WebSocketSessionManager.unsubscribeFromConversation(convId, session);

        // 发送成功响应
        JSONObject response = new JSONObject();
        response.put("action", "unsubscribed");
        response.put("convId", convId);
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());

        session.sendMessage(new TextMessage(response.toJSONString()));

        System.out.println("✅ 用户 " + userId + " 成功取消订阅会话 " + convId);
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("pong"));
        System.out.println("💓 心跳响应: pong");
    }

    // ========== Token验证相关方法 ==========

    /**
     * 验证WebSocket Token并返回用户ID
     */
    private Long validateWebSocketToken(String token) {
        try {
            // 1. 基本检查
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token为空");
            }

            // 2. 快速验证Token（检查签名和过期）
            if (!jwtUtil.validateTokenQuick(token)) {
                throw new IllegalArgumentException("Token无效或已过期");
            }

            // 3. 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new IllegalArgumentException("无法从Token解析用户ID");
            }

            return userId;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new IllegalArgumentException("Token已过期");
        } catch (io.jsonwebtoken.SignatureException e) {
            throw new IllegalArgumentException("Token签名无效");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new IllegalArgumentException("Token格式错误");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("用户ID格式错误");
        } catch (Exception e) {
            throw new IllegalArgumentException("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 从WebSocketSession提取token：优先URL参数 token=xxx，其次握手请求的 Cookie 中的 token
     */
    private String extractTokenFromSession(WebSocketSession session) {
        try {
            // 1. 先尝试 URL 查询参数 token=xxx
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        String[] parts = param.split("=", 2);
                        if (parts.length == 2) {
                            String token = parts[1];
                            if (token.contains("%")) {
                                token = java.net.URLDecoder.decode(token, "UTF-8");
                            }
                            if (!token.isEmpty()) {
                                return token;
                            }
                        }
                    }
                }
            }

            // 2. 再尝试从握手请求的 Cookie 中读取 token（适配 HttpOnly Cookie 登录）
            String cookieHeader = session.getHandshakeHeaders().getFirst("Cookie");
            if (cookieHeader != null && !cookieHeader.isEmpty()) {
                String token = parseTokenFromCookieHeader(cookieHeader);
                if (token != null && !token.isEmpty()) {
                    return token;
                }
            }

            System.err.println("WebSocket连接没有查询参数且Cookie中无token");
            return null;

        } catch (Exception e) {
            System.err.println("提取token异常: " + e.getMessage());
            return null;
        }
    }

    /** 从 Cookie 头字符串中解析 token 的值（格式: name=value; token=xxx; ...） */
    private String parseTokenFromCookieHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.startsWith("token=")) {
                String value = trimmed.substring(6).trim();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    /**
     * 验证用户是否在会话中
     */
    private boolean isUserInConversation(Long userId, Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            for (ConversationMember member : members) {
                if (member.getUserId().equals(userId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("验证用户会话成员关系失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送连接成功响应
     */
    private void sendConnectionSuccess(WebSocketSession session, Long userId, List<Long> subscriptions) throws Exception {
        JSONObject response = new JSONObject();
        response.put("action", "connected");
        response.put("userId", userId);
        response.put("subscriptions", subscriptions);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "WebSocket连接成功，已订阅 " +
                (subscriptions != null ? subscriptions.size() : 0) + " 个会话");

        session.sendMessage(new TextMessage(response.toJSONString()));
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(WebSocketSession session, String errorMessage) {
        try {
            JSONObject errorJson = new JSONObject();
            errorJson.put("action", "error");
            errorJson.put("message", errorMessage);
            errorJson.put("timestamp", System.currentTimeMillis());

            session.sendMessage(new TextMessage(errorJson.toJSONString()));

            System.err.println("❌ 发送错误响应: " + errorMessage);
        } catch (Exception e) {
            System.err.println("发送错误响应失败: " + e.getMessage());
        }
    }

    /**
     * 关闭连接并发送错误信息
     */
    private void closeWithError(WebSocketSession session, String reason) {
        try {
            System.err.println("关闭连接，原因: " + reason);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason(reason));
        } catch (Exception e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
    }
}
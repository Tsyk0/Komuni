package hrc.komuni.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.FileAttachment;
import hrc.komuni.entity.Message;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.manager.WebSocketSessionManager;
import hrc.komuni.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private FileAttachmentService fileAttachmentService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageReadStatusService messageReadStatusService;

    @Autowired
    private ConversationMemberService conversationMemberService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long convId = getConvIdFromSession(session);

        System.out.println("用户 " + userId + " 尝试连接到会话 " + convId);

        // 验证会话是否存在
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        if (conversation == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("会话不存在"));
            System.err.println("会话 " + convId + " 不存在");
            return;
        }

        // 验证用户是否在该会话中
        if (!validateUserInConversation(userId, convId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户不在该会话中"));
            System.err.println("用户 " + userId + " 不在会话 " + convId + " 中");
            return;
        }

        // 将用户和会话保存到 WebSocketSessionManager
        WebSocketSessionManager.addUserSession(userId, session);
        WebSocketSessionManager.addConvSession(convId, session);

        // 更新用户在线状态
        userService.updateOnlineStatus(userId, 1);

        // 更新最后阅读时间
        conversationMemberService.updateLastReadTime(convId, userId);

        // 获取会话成员数量
        List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
        System.out.println("✅ 用户 " + userId + " 已连接到会话 " + convId + " (成员数: " + members.size() + ")");

        // 调试：显示所有成员
        for (ConversationMember member : members) {
            System.out.println("  成员: userId=" + member.getUserId() + ", status=" + member.getMemberStatus());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📨 收到消息：" + payload);

        // 处理心跳消息
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        try {
            // 解析消息JSON
            JSONObject messageJson = JSON.parseObject(payload);
            String action = messageJson.getString("action");

            // 根据action处理不同类型的消息
            switch (action) {
                case "sendMessage":
                    handleSendMessage(session, messageJson);
                    break;
                case "readMessage":
                    handleReadMessage(session, messageJson);
                    break;
                case "recallMessage":
                    handleRecallMessage(session, messageJson);
                    break;
                case "sendFileMessage":
                    handleFileMessage(session, messageJson);
                    break;

                default:
                    sendErrorResponse(session, "未知的操作类型: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(session, "消息处理失败: " + e.getMessage());
        }
    }

    private void handleReadMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long messageId = messageJson.getLong("messageId");
        Long convId = messageJson.getLong("convId");

        // 查询会话信息
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        if (conversation == null) {
            sendErrorResponse(session, "会话不存在");
            return;
        }

        // 如果是群聊，检查是否启用已读回执
        if (conversation.getConvType() == 2) {
            Boolean enableReadReceipt = conversation.getEnableReadReceipt();
            if (enableReadReceipt == null || !enableReadReceipt) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("action", "readReceiptDisabled");
                responseJson.put("message", "该群聊已禁用消息已读回执");
                session.sendMessage(new TextMessage(responseJson.toJSONString()));
                return;
            }
        }

        // 统一使用 message_read_status 表记录已读
        messageReadStatusService.markMessageAsRead(messageId, userId);

        // 更新用户的最后阅读时间
        conversationMemberService.updateLastReadTime(convId, userId);

        // 构建响应
        JSONObject responseJson = new JSONObject();
        responseJson.put("action", "messageRead");
        responseJson.put("messageId", messageId);

        if (conversation.getConvType() == 2) {
            // 群聊：返回已读人数
            Integer readCount = messageReadStatusService.getReadUserCount(messageId);
            Integer totalMembers = conversationService.getMemberCount(convId);
            responseJson.put("readCount", readCount);
            responseJson.put("totalMembers", totalMembers);

            // 推送给群成员（显示已读人数更新）
            broadcastToConversationExcludingSender(convId, responseJson, userId);
        } else {
            // 单聊：返回是否已读（2人已读表示双方都已读）
            Integer readCount = messageReadStatusService.getReadUserCount(messageId);
            responseJson.put("isRead", readCount >= 2);
        }

        session.sendMessage(new TextMessage(responseJson.toJSONString()));
    }

    /**
     * 处理发送消息 - 简化版本，不再需要序列号
     */
    private void handleSendMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long senderId = getUserIdFromSession(session);
        Long convId = messageJson.getLong("convId");
        String messageType = messageJson.getString("messageType");
        String messageContent = messageJson.getString("messageContent");
        Long replyToMessageId = messageJson.getLong("replyToMessageId");

        System.out.println("=== 📤 处理发送消息（无序列号版本） ===");
        System.out.println("发送者: " + senderId + ", 会话: " + convId + ", 类型: " + messageType);

        // 验证会话是否存在
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        if (conversation == null) {
            System.err.println("❌ 会话不存在: " + convId);
            sendErrorResponse(session, "会话不存在");
            return;
        }

        System.out.println("✅ 会话存在: " + conversation.getConvName());

        // 获取会话成员信息（用于调试）
        List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
        System.out.println("👥 会话成员: " + members.size() + " 人");

        // 创建消息对象 - 不再需要序列号！
        Message message = new Message();
        message.setConvId(convId);
        message.setSenderId(senderId);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setMessageContent(messageContent);
        message.setMessageStatus(0); // 0-发送中
        message.setIsRecalled(false);
        message.setReplyToMessageId(replyToMessageId);
        message.setSendTime(new Date()); // 使用当前时间作为唯一顺序标识

        // 保存消息到数据库
        Long messageId = messageService.insertMessage(message);
        if (messageId == null) {
            System.err.println("❌ 消息保存失败");
            sendErrorResponse(session, "消息保存失败");
            return;
        }

        System.out.println("✅ 消息保存成功，消息ID: " + messageId + ", 时间: " + message.getSendTime());

        // 更新消息状态为已发送
        message.setMessageId(messageId);
        message.setMessageStatus(1); // 1-已发送
        messageService.updateMessageStatus(messageId, 1);

        // 构建返回给客户端的消息对象 - 不再包含convMsgSeq
        JSONObject responseJson = new JSONObject();
        responseJson.put("action", "messageSent");
        responseJson.put("messageId", messageId);
        responseJson.put("convId", convId);
        responseJson.put("senderId", senderId);
        responseJson.put("messageType", message.getMessageType());
        responseJson.put("messageContent", messageContent);
        responseJson.put("sendTime", message.getSendTime().getTime()); // 使用时间戳
        responseJson.put("messageStatus", 1);
        // 不再有convMsgSeq字段！

        // 发送确认消息给发送者
        System.out.println("📤 发送确认消息给发送者...");
        session.sendMessage(new TextMessage(responseJson.toJSONString()));

        // 构建广播消息
        JSONObject broadcastJson = new JSONObject();
        broadcastJson.put("action", "newMessage");
        broadcastJson.putAll(responseJson); // 复制所有字段
        broadcastJson.remove("action"); // 移除原来的action
        broadcastJson.put("action", "newMessage"); // 重新设置为newMessage

        System.out.println("=== 🚀 开始广播消息 ===");
        System.out.println("广播消息内容: " + broadcastJson.toJSONString());

        // 广播给会话所有其他成员
        int broadcastCount = broadcastToConversationExcludingSender(convId, broadcastJson, senderId);

        System.out.println("=== 🎯 广播完成 ===");
        System.out.println("成功推送给 " + broadcastCount + " 个其他用户");

        // 根据会话类型更新消息状态
        if (conversation.getConvType() == 1) {
            // 单聊：如果有接收者在线，更新为已送达
            if (broadcastCount > 0) {
                messageService.updateMessageStatus(messageId, 2); // 2-已送达
                System.out.println("💌 单聊消息已送达，messageId: " + messageId);
            } else {
                System.out.println("⏳ 单聊接收者不在线，messageId: " + messageId);
            }
        } else {
            // 群聊：保持为已发送状态
            System.out.println("👥 群聊消息已推送给 " + broadcastCount + " 个在线用户");
        }

        // 更新未读计数（这是已有的逻辑）
        System.out.println("📊 更新未读计数...");
    }

    /**
     * 简化的广播方法 - 确保一定会尝试广播
     */
    private int broadcastToConversationExcludingSender(Long convId, JSONObject messageJson, Long excludeUserId) {
        int deliveredCount = 0;

        try {
            System.out.println("=== 🚀 开始广播消息 ===");
            System.out.println("📌 广播到会话: " + convId);
            System.out.println("📌 排除用户: " + excludeUserId);

            // 获取会话所有成员
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            System.out.println("📌 会话成员总数: " + members.size());

            if (members.isEmpty()) {
                System.out.println("⚠️ 会话没有成员，无法广播");
                return 0;
            }

            // 详细列出所有成员
            for (int i = 0; i < members.size(); i++) {
                ConversationMember member = members.get(i);
                System.out.println("👤 成员 " + (i+1) + ": userId=" + member.getUserId() +
                        ", status=" + member.getMemberStatus());
            }

            String messageStr = messageJson.toJSONString();

            for (ConversationMember member : members) {
                Long memberUserId = member.getUserId();

                // 排除发送者
                if (memberUserId.equals(excludeUserId)) {
                    System.out.println("⏭️ 跳过发送者: " + memberUserId);
                    continue;
                }

                // 获取该用户的所有WebSocket会话
                Set<WebSocketSession> memberSessions = WebSocketSessionManager.getUserSessions(memberUserId);

                if (memberSessions == null || memberSessions.isEmpty()) {
                    System.out.println("⚠️ 用户 " + memberUserId + " 没有WebSocket连接");
                    continue;
                }

                System.out.println("🔗 用户 " + memberUserId + " 有 " + memberSessions.size() + " 个WebSocket连接");

                boolean userDelivered = false;
                for (WebSocketSession memberSession : memberSessions) {
                    if (memberSession.isOpen()) {
                        try {
                            System.out.println("  📤 正在推送消息到用户 " + memberUserId + "...");
                            memberSession.sendMessage(new TextMessage(messageStr));
                            userDelivered = true;
                            System.out.println("  ✅ 消息已推送到用户 " + memberUserId);
                            break;
                        } catch (IOException e) {
                            System.err.println("  ❌ 推送消息失败: " + e.getMessage());
                        }
                    }
                }

                if (userDelivered) {
                    deliveredCount++;
                }
            }

            System.out.println("=== 🎯 广播完成 ===");
            System.out.println("✅ 成功推送: " + deliveredCount + " 个成员");

        } catch (Exception e) {
            System.err.println("❌ 广播消息异常: " + e.getMessage());
            e.printStackTrace();
        }

        return deliveredCount;
    }

    /**
     * 处理撤回消息
     */
    private void handleRecallMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long messageId = messageJson.getLong("messageId");
        Long convId = messageJson.getLong("convId");

        // 撤回消息
        int result = messageService.recallMessage(messageId, userId);
        if (result > 0) {
            // 构建撤回通知
            JSONObject recallJson = new JSONObject();
            recallJson.put("action", "messageRecalled");
            recallJson.put("messageId", messageId);
            recallJson.put("convId", convId);

            // 广播给会话所有成员（除了撤回者）
            broadcastToConversationExcludingSender(convId, recallJson, userId);
        } else {
            sendErrorResponse(session, "撤回消息失败");
        }
    }

    /**
     * 验证用户是否在会话中
     */
    private boolean validateUserInConversation(Long userId, Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            for (ConversationMember member : members) {
                if (member.getUserId().equals(userId) && member.getMemberStatus() == 1) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("验证用户是否在会话中失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(WebSocketSession session, String errorMsg) {
        try {
            JSONObject errorJson = new JSONObject();
            errorJson.put("action", "error");
            errorJson.put("message", errorMsg);
            session.sendMessage(new TextMessage(errorJson.toJSONString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long convId = getConvIdFromSession(session);

        // 移除用户和会话的会话信息
        WebSocketSessionManager.removeUserSession(userId, session);
        WebSocketSessionManager.removeConvSession(convId, session);

        // 更新用户在线状态（如果该用户没有其他会话）
        Set<WebSocketSession> remainingSessions = WebSocketSessionManager.getUserSessions(userId);
        if (remainingSessions == null || remainingSessions.isEmpty()) {
            userService.updateOnlineStatus(userId, 0);
            System.out.println("用户 " + userId + " 已离线");
        } else {
            System.out.println("用户 " + userId + " 仍有 " + remainingSessions.size() + " 个活跃连接");
        }

        System.out.println("用户 " + userId + " 已断开连接，会话: " + convId + ", 原因: " + status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        exception.printStackTrace();
    }

    // 从 WebSocketSession 中提取 userId
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return Long.parseLong(param.split("=")[1]);
                }
            }
            throw new IllegalArgumentException("未找到userId参数");
        } catch (Exception e) {
            throw new IllegalArgumentException("解析userId失败: " + e.getMessage());
        }
    }

    // 从 WebSocketSession 中提取 convId
    private Long getConvIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("convId=")) {
                    return Long.parseLong(param.split("=")[1]);
                }
            }
            throw new IllegalArgumentException("未找到convId参数");
        } catch (Exception e) {
            throw new IllegalArgumentException("解析convId失败: " + e.getMessage());
        }
    }

    /**
     * 处理文件消息 - 简化版本，不再需要序列号
     */
    private void handleFileMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long senderId = getUserIdFromSession(session);
        Long convId = messageJson.getLong("convId");
        String fileName = messageJson.getString("fileName");
        String filePath = messageJson.getString("filePath");
        Long fileSize = messageJson.getLong("fileSize");
        String fileType = messageJson.getString("fileType");

        // 验证会话是否存在
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        if (conversation == null) {
            sendErrorResponse(session, "会话不存在");
            return;
        }

        // 创建消息对象 - 不再需要序列号
        Message message = new Message();
        message.setConvId(convId);
        message.setSenderId(senderId);
        message.setMessageType("file");
        message.setMessageContent(fileName);
        message.setMessageStatus(0); // 0-发送中
        message.setIsRecalled(false);
        message.setSendTime(new Date());

        // 保存消息到数据库
        Long messageId = messageService.insertMessage(message);
        if (messageId == null) {
            sendErrorResponse(session, "消息保存失败");
            return;
        }

        // 创建文件附件记录
        FileAttachment attachment = new FileAttachment();
        attachment.setMessageId(messageId);
        attachment.setUploaderId(senderId);
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setFilePath(filePath);
        attachment.setUploadTime(new Date());

        // 保存附件信息到数据库
        int attachmentResult = fileAttachmentService.insertFileAttachment(attachment);
        if (attachmentResult <= 0) {
            sendErrorResponse(session, "文件附件保存失败");
            return;
        }

        // 更新消息状态为已发送
        message.setMessageId(messageId);
        message.setMessageStatus(1); // 1-已发送
        messageService.updateMessageStatus(messageId, 1);

        // 构建返回给客户端的消息对象 - 不再包含convMsgSeq
        JSONObject responseJson = new JSONObject();
        responseJson.put("action", "fileMessageSent");
        responseJson.put("messageId", messageId);
        responseJson.put("convId", convId);
        responseJson.put("senderId", senderId);
        responseJson.put("messageType", "file");
        responseJson.put("messageContent", fileName);
        responseJson.put("fileInfo", attachment);
        responseJson.put("sendTime", message.getSendTime().getTime());
        responseJson.put("messageStatus", 1);

        // 发送确认消息给发送者
        session.sendMessage(new TextMessage(responseJson.toJSONString()));

        // 构建广播消息
        JSONObject broadcastJson = new JSONObject();
        broadcastJson.put("action", "newFileMessage");
        broadcastJson.putAll(responseJson);

        // 广播给会话所有其他成员
        int broadcastCount = broadcastToConversationExcludingSender(convId, broadcastJson, senderId);

        // 根据会话类型更新消息状态
        if (conversation.getConvType() == 1 && broadcastCount > 0) {
            messageService.updateMessageStatus(messageId, 2); // 2-已送达
        }

        System.out.println("文件消息处理完成，messageId: " + messageId + ", 推送用户数: " + broadcastCount);
    }
}
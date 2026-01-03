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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        Long convId = getConvIdFromSession(session);

        // 验证用户是否在该会话中
        if (!validateUserInConversation(userId, convId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户不在该会话中"));
            return;
        }

        // 将用户和会话保存到 WebSocketSessionManager
        WebSocketSessionManager.addUserSession(userId, session);
        WebSocketSessionManager.addConvSession(convId, session);

        // 更新用户在线状态
        userService.updateOnlineStatus(userId, 1);

        // 更新最后阅读时间
        conversationService.updateLastReadTime(convId, userId);

        System.out.println("用户 " + userId + " 已连接到会话 " + convId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息：" + payload);

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
            // 群聊：检查是否启用已读回执
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
        conversationService.updateLastReadTime(convId, userId);

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
            List<ConversationMember> members = conversationService.getMembersByConvId(convId);
            for (ConversationMember member : members) {
                if (!member.getUserId().equals(userId)) {
                    Set<WebSocketSession> memberSessions = WebSocketSessionManager.getUserSessions(member.getUserId());
                    for (WebSocketSession memberSession : memberSessions) {
                        if (memberSession.isOpen()) {
                            memberSession.sendMessage(new TextMessage(responseJson.toJSONString()));
                        }
                    }
                }
            }
        } else {
            // 单聊：返回是否已读（2人已读表示双方都已读）
            Integer readCount = messageReadStatusService.getReadUserCount(messageId);
            responseJson.put("isRead", readCount >= 2);
        }

        session.sendMessage(new TextMessage(responseJson.toJSONString()));
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long senderId = getUserIdFromSession(session);
        Long convId = messageJson.getLong("convId");
        String messageType = messageJson.getString("messageType");
        String messageContent = messageJson.getString("messageContent");
        Long receiverId = messageJson.getLong("receiverId"); // 单聊时使用
        Long replyToMessageId = messageJson.getLong("replyToMessageId"); // 回复消息ID

        // 验证会话是否存在
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        if (conversation == null) {
            sendErrorResponse(session, "会话不存在");
            return;
        }

        // 创建消息对象
        Message message = new Message();
        message.setConvId(convId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setMessageContent(messageContent);
        message.setMessageStatus(0); // 0-发送中
        message.setIsRecalled(false);
        message.setReplyToMessageId(replyToMessageId);
        message.setSendTime(new Date());

        // 保存消息到数据库
        Long messageId = messageService.insertMessage(message);
        if (messageId == null) {
            sendErrorResponse(session, "消息保存失败");
            return;
        }

        // 更新消息状态为已发送
        message.setMessageId(messageId);
        message.setMessageStatus(1); // 1-已发送
        messageService.updateMessageStatus(messageId, 1); // 修正：只传两个参数

        // 构建返回给客户端的消息对象
        JSONObject responseJson = new JSONObject();
        responseJson.put("action", "messageSent");
        responseJson.put("messageId", messageId);
        responseJson.put("convId", convId);
        responseJson.put("senderId", senderId);
        responseJson.put("messageType", message.getMessageType());
        responseJson.put("messageContent", messageContent);
        responseJson.put("sendTime", message.getSendTime().getTime());
        responseJson.put("messageStatus", 1);

        // 发送确认消息给发送者
        session.sendMessage(new TextMessage(responseJson.toJSONString()));

        // 根据会话类型推送消息
        Integer convType = conversation.getConvType();
        if (convType == 1) {
            // 单聊：推送给接收者
            pushMessageToSingleChat(convId, senderId, receiverId, responseJson);
        } else {
            // 群聊：推送给所有成员（除了发送者）
            pushMessageToGroupChat(convId, senderId, responseJson);
        }
    }

    /**
     * 单聊消息推送
     */
    private void pushMessageToSingleChat(Long convId, Long senderId, Long receiverId, JSONObject messageJson) {
        // 获取接收者的所有WebSocket会话
        Set<WebSocketSession> receiverSessions = WebSocketSessionManager.getUserSessions(receiverId);

        // 构建推送消息
        JSONObject pushJson = new JSONObject();
        pushJson.put("action", "newMessage");
        pushJson.putAll(messageJson);

        boolean delivered = false;
        for (WebSocketSession receiverSession : receiverSessions) {
            if (receiverSession.isOpen()) {
                try {
                    receiverSession.sendMessage(new TextMessage(pushJson.toJSONString()));
                    delivered = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 更新消息状态为已送达
        Long messageId = messageJson.getLong("messageId");
        if (delivered) {
            messageService.updateMessageStatus(messageId, 2); // 修正：只传两个参数
        }
    }

    /**
     * 群聊消息推送
     */
    private void pushMessageToGroupChat(Long convId, Long senderId, JSONObject messageJson) {
        // 获取会话的所有成员
        List<ConversationMember> members = conversationService.getMembersByConvId(convId);

        // 构建推送消息
        JSONObject pushJson = new JSONObject();
        pushJson.put("action", "newMessage");
        pushJson.putAll(messageJson);

        int deliveredCount = 0;
        for (ConversationMember member : members) {
            // 不推送给发送者
            if (member.getUserId().equals(senderId)) {
                continue;
            }

            // 获取成员的WebSocket会话
            Set<WebSocketSession> memberSessions = WebSocketSessionManager.getUserSessions(member.getUserId());
            for (WebSocketSession memberSession : memberSessions) {
                if (memberSession.isOpen()) {
                    try {
                        memberSession.sendMessage(new TextMessage(pushJson.toJSONString()));
                        deliveredCount++;
                        break; // 每个用户只需要推送一次
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 群聊消息状态保持为已发送（已送达状态需要客户端确认）
        System.out.println("群聊消息已推送给 " + deliveredCount + " 个在线用户");
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

            // 推送给会话所有成员
            Conversation conversation = conversationService.selectConversationByConvId(convId);
            if (conversation.getConvType() == 1) {
                // 单聊：推送给对方
                Message message = messageService.selectMessageByMessageId(messageId);
                Long receiverId = message.getReceiverId();
                Set<WebSocketSession> receiverSessions = WebSocketSessionManager.getUserSessions(receiverId);
                for (WebSocketSession receiverSession : receiverSessions) {
                    if (receiverSession.isOpen()) {
                        receiverSession.sendMessage(new TextMessage(recallJson.toJSONString()));
                    }
                }
            } else {
                // 群聊：推送给所有成员
                List<ConversationMember> members = conversationService.getMembersByConvId(convId);
                for (ConversationMember member : members) {
                    Set<WebSocketSession> memberSessions = WebSocketSessionManager.getUserSessions(member.getUserId());
                    for (WebSocketSession memberSession : memberSessions) {
                        if (memberSession.isOpen()) {
                            memberSession.sendMessage(new TextMessage(recallJson.toJSONString()));
                        }
                    }
                }
            }
        } else {
            sendErrorResponse(session, "撤回消息失败");
        }
    }

    /**
     * 验证用户是否在会话中
     */
    private boolean validateUserInConversation(Long userId, Long convId) {
        List<ConversationMember> members = conversationService.getMembersByConvId(convId);
        for (ConversationMember member : members) {
            if (member.getUserId().equals(userId) && member.getMemberStatus() == 1) {
                return true;
            }
        }
        return false;
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
        }

        System.out.println("用户 " + userId + " 已断开连接");
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

    // 在WebSocketHandler.java中添加处理文件消息的方法
    private void handleFileMessage(WebSocketSession session, JSONObject messageJson) throws Exception {
        Long senderId = getUserIdFromSession(session);
        Long convId = messageJson.getLong("convId");
        String fileName = messageJson.getString("fileName");
        String filePath = messageJson.getString("filePath");
        Long fileSize = messageJson.getLong("fileSize");
        String fileType = messageJson.getString("fileType");

        // 创建消息对象
        Message message = new Message();
        message.setConvId(convId);
        message.setSenderId(senderId);
        message.setReceiverId(messageJson.getLong("receiverId")); // 单聊时使用
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

        // 构建返回给客户端的消息对象
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

        // 根据会话类型推送消息
        Conversation conversation = conversationService.selectConversationByConvId(convId);
        Integer convType = conversation.getConvType();
        if (convType == 1) {
            // 单聊：推送给接收者
            pushFileMessageToSingleChat(convId, senderId, message.getReceiverId(), responseJson);
        } else {
            // 群聊：推送给所有成员（除了发送者）
            pushFileMessageToGroupChat(convId, senderId, responseJson);
        }
    }

    // 单聊文件消息推送
    private void pushFileMessageToSingleChat(Long convId, Long senderId, Long receiverId, JSONObject messageJson) {
        Set<WebSocketSession> receiverSessions = WebSocketSessionManager.getUserSessions(receiverId);

        JSONObject pushJson = new JSONObject();
        pushJson.put("action", "newFileMessage");
        pushJson.putAll(messageJson);

        boolean delivered = false;
        for (WebSocketSession receiverSession : receiverSessions) {
            if (receiverSession.isOpen()) {
                try {
                    receiverSession.sendMessage(new TextMessage(pushJson.toJSONString()));
                    delivered = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Long messageId = messageJson.getLong("messageId");
        if (delivered) {
            messageService.updateMessageStatus(messageId, 2); // 2-已送达
        }
    }

    // 群聊文件消息推送
    private void pushFileMessageToGroupChat(Long convId, Long senderId, JSONObject messageJson) {
        List<ConversationMember> members = conversationService.getMembersByConvId(convId);

        JSONObject pushJson = new JSONObject();
        pushJson.put("action", "newFileMessage");
        pushJson.putAll(messageJson);

        int deliveredCount = 0;
        for (ConversationMember member : members) {
            if (member.getUserId().equals(senderId)) {
                continue;
            }

            Set<WebSocketSession> memberSessions = WebSocketSessionManager.getUserSessions(member.getUserId());
            for (WebSocketSession memberSession : memberSessions) {
                if (memberSession.isOpen()) {
                    try {
                        memberSession.sendMessage(new TextMessage(pushJson.toJSONString()));
                        deliveredCount++;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("群聊文件消息已推送给 " + deliveredCount + " 个在线用户");
    }

}

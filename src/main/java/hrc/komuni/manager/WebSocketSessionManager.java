package hrc.komuni.manager;

import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理用户连接和消息订阅关系
 */
public class WebSocketSessionManager {

    // ========== 核心数据存储 ==========

    // 会话ID → 该会话的所有订阅连接（用于广播消息）
    private static final Map<Long, Set<WebSocketSession>> conversationSubscribers = new ConcurrentHashMap<>();

    // 用户ID → 用户的WebSocket连接（单设备）
    private static final Map<Long, WebSocketSession> userConnections = new ConcurrentHashMap<>();

    // WebSocket连接 → 用户ID（反向查找）
    private static final Map<WebSocketSession, Long> connectionUsers = new ConcurrentHashMap<>();

    // WebSocket连接 → 该连接订阅的会话集合
    private static final Map<WebSocketSession, Set<Long>> connectionSubscriptions = new ConcurrentHashMap<>();

    // ========== 连接管理方法 ==========

    /**
     * 添加用户连接
     * @param userId 用户ID
     * @param session WebSocket连接
     */
    public static void addUserConnection(Long userId, WebSocketSession session) {
        // 如果用户已有连接，先关闭旧的（单设备模式）
        WebSocketSession oldSession = userConnections.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close();
                System.out.println("🔄 关闭用户 " + userId + " 的旧连接（新连接建立）");
            } catch (Exception e) {
                // 忽略关闭错误
            }
            cleanupConnection(oldSession);
        }

        // 添加新连接
        userConnections.put(userId, session);
        connectionUsers.put(session, userId);
        connectionSubscriptions.put(session, new HashSet<>());

        System.out.println("✅ 用户 " + userId + " WebSocket连接建立");
    }

    /**
     * 移除用户连接（连接关闭时调用）
     * @param session WebSocket连接
     */
    public static void removeConnection(WebSocketSession session) {
        Long userId = connectionUsers.get(session);

        if (userId != null) {
            // 从用户连接映射中移除
            WebSocketSession userSession = userConnections.get(userId);
            if (session.equals(userSession)) {
                userConnections.remove(userId);
                System.out.println("👤 用户 " + userId + " 连接已移除");
            }
        }

        // 从所有会话的订阅者中移除这个连接
        Set<Long> subscriptions = connectionSubscriptions.get(session);
        if (subscriptions != null) {
            for (Long convId : subscriptions) {
                removeSubscriber(convId, session);
            }
        }

        // 清理其他映射
        connectionUsers.remove(session);
        connectionSubscriptions.remove(session);

        System.out.println("🗑️  WebSocket连接清理完成");
    }

    // ========== 订阅管理方法 ==========

    /**
     * 用户订阅会话（开始接收该会话的消息）
     * @param convId 会话ID
     * @param session 用户的WebSocket连接
     */
    public static void subscribeToConversation(Long convId, WebSocketSession session) {
        // 添加到会话的订阅者列表
        conversationSubscribers.computeIfAbsent(convId, k -> new HashSet<>()).add(session);

        // 记录连接订阅了哪些会话
        Set<Long> subscriptions = connectionSubscriptions.get(session);
        if (subscriptions != null) {
            subscriptions.add(convId);
        }

        System.out.println("📌 会话 " + convId + " 新增订阅者，当前订阅数: " +
                conversationSubscribers.get(convId).size());
    }

    /**
     * 用户取消订阅会话（停止接收该会话的消息）
     * @param convId 会话ID
     * @param session 用户的WebSocket连接
     */
    public static void unsubscribeFromConversation(Long convId, WebSocketSession session) {
        removeSubscriber(convId, session);

        // 从连接的订阅记录中移除
        Set<Long> subscriptions = connectionSubscriptions.get(session);
        if (subscriptions != null) {
            subscriptions.remove(convId);
        }

        System.out.println("📌 会话 " + convId + " 移除订阅者");
    }

    /**
     * 获取会话的所有订阅连接（用于广播消息）
     * @param convId 会话ID
     * @return 订阅该会话的所有WebSocket连接
     */
    public static Set<WebSocketSession> getConversationSubscribers(Long convId) {
        return conversationSubscribers.getOrDefault(convId, new HashSet<>());
    }

    /**
     * 获取用户的WebSocket连接
     * @param userId 用户ID
     * @return 用户的WebSocket连接
     */
    public static WebSocketSession getUserConnection(Long userId) {
        return userConnections.get(userId);
    }

    /**
     * 获取连接对应的用户ID
     * @param session WebSocket连接
     * @return 用户ID
     */
    public static Long getUserIdByConnection(WebSocketSession session) {
        return connectionUsers.get(session);
    }

    /**
     * 获取连接订阅的所有会话
     * @param session WebSocket连接
     * @return 会话ID集合
     */
    public static Set<Long> getConnectionSubscriptions(WebSocketSession session) {
        return connectionSubscriptions.getOrDefault(session, new HashSet<>());
    }

    // ========== 内部辅助方法 ==========

    /**
     * 从会话订阅者中移除连接
     */
    private static void removeSubscriber(Long convId, WebSocketSession session) {
        Set<WebSocketSession> subscribers = conversationSubscribers.get(convId);
        if (subscribers != null) {
            subscribers.remove(session);
            if (subscribers.isEmpty()) {
                conversationSubscribers.remove(convId);
            }
        }
    }

    /**
     * 清理连接的所有订阅
     */
    private static void cleanupConnection(WebSocketSession session) {
        Set<Long> subscriptions = connectionSubscriptions.get(session);
        if (subscriptions != null) {
            for (Long convId : subscriptions) {
                removeSubscriber(convId, session);
            }
        }
        connectionSubscriptions.remove(session);
        connectionUsers.remove(session);
    }

    /**
     * 强制用户下线
     * @param userId 用户ID
     */
    public static void forceLogoutUser(Long userId) {
        WebSocketSession session = userConnections.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
                System.out.println("🚫 强制用户 " + userId + " 下线");
            } catch (Exception e) {
                System.err.println("强制下线失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取统计信息（用于监控和调试）
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineUsers", userConnections.size());
        stats.put("activeConversations", conversationSubscribers.size());
        stats.put("totalConnections", connectionUsers.size());

        // 会话订阅统计
        Map<Long, Integer> conversationStats = new HashMap<>();
        conversationSubscribers.forEach((convId, subscribers) -> {
            conversationStats.put(convId, subscribers.size());
        });
        stats.put("conversationSubscriberCounts", conversationStats);

        return stats;
    }
}
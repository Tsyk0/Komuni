package hrc.komuni.manager;

import org.springframework.web.socket.WebSocketSession;

import java.util.*;

public class WebSocketSessionManager {

    // 存储会话与群组之间的映射
    private static final Map<Long, Set<WebSocketSession>> convSessionMap = new HashMap<>();

    // 存储会话与用户之间的映射
    private static final Map<Long, Set<WebSocketSession>> userSessionMap = new HashMap<>();

    // 添加群组会话
    public static void addConvSession(long convId, WebSocketSession session) {
        convSessionMap.computeIfAbsent(convId, k -> new HashSet<>()).add(session);
    }

    // 获取群组成员的会话
    public static Set<WebSocketSession> getConvSessions(long convId) {
        return convSessionMap.getOrDefault(convId, new HashSet<>());
    }

    // 移除群组会话
    public static void removeConvSession(long convId, WebSocketSession session) {
        Set<WebSocketSession> sessions = convSessionMap.get(convId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                convSessionMap.remove(convId);
            }
        }
    }

    // 添加用户会话
    public static void addUserSession(long userId, WebSocketSession session) {
        userSessionMap.computeIfAbsent(userId, k -> new HashSet<>()).add(session);
    }

    // 获取用户会话
    public static Set<WebSocketSession> getUserSessions(long userId) {
        return userSessionMap.getOrDefault(userId, new HashSet<>());
    }

    // 移除用户会话
    public static void removeUserSession(long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessionMap.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessionMap.remove(userId);
            }
        }
    }
}

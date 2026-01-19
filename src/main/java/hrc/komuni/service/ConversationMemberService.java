package hrc.komuni.service;

import hrc.komuni.entity.ConversationMember;
import java.util.List;

public interface ConversationMemberService {

    // 1. 查询相关方法
    ConversationMember selectByConvIdAndUserId(Long convId, Long userId);
    List<ConversationMember> selectMembersByConvId(Long convId);
    List<ConversationMember> selectByUserId(Long userId);
    Long getLastReadSeq(Long userId, Long convId);
    String getPrivateDisplayName(Long convId, Long userId);
    List<Long> selectConvIdsByUserId(Long userId);

    // 2. 插入相关方法
    int insertConversationMember(Long convId, Long userId);

    // 3. 更新相关方法
    int updateConversationMember(ConversationMember member);
    int updatePrivateDisplayName(Long convId, Long userId, String displayName);
    int updateLastReadTime(Long convId, Long userId);
    int updateLastReadSeq(Long userId, Long convId, Long seq);
    int incrementUnreadCount(Long convId, Long excludeUserId);
    int removeMember(Long convId, Long userId);
    int incrementMemberCount(Long convId);

    int decrementMemberCount(Long convId);
    // 4. 未读消息相关方法
    int getUnreadCount(Long convId, Long userId);
    int updateUnreadCount(Long convId, Long userId);
    int updateAllMembersUnreadCount(Long convId);
    public int setUnreadCountZero(Long convId, Long userId);

    // 5. 会话创建相关方法
    Long createSingleConversation(Long user1Id, Long user2Id);
    Long createGroupConversation(Long ownerId, String convName);
    int addMemberToConversation(Long convId, Long userId, String memberNickname);

    // ============ 未在Controller中使用的方法（放在底端） ============
}
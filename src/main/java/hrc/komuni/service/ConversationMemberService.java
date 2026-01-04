package hrc.komuni.service;

import hrc.komuni.entity.ConversationMember;
import java.util.List;

public interface ConversationMemberService {

    /**
     * 根据会话ID和用户ID查询会话成员
     */
    ConversationMember selectByConvIdAndUserId(Long convId, Long userId);

    /**
     * 根据会话ID查询所有成员
     */
    List<ConversationMember> selectMembersByConvId(Long convId);

    /**
     * 根据用户ID查询用户所在的所有会话
     */
    List<ConversationMember> selectByUserId(Long userId);

    /**
     * 添加会话成员
     */
    int insertConversationMember(ConversationMember member);

    /**
     * 更新会话成员信息
     */
    int updateConversationMember(ConversationMember member);

    /**
     * 更新单聊中的对方显示名称
     */
    int updatePrivateDisplayName(Long convId, Long userId, String displayName);

    /**
     * 更新成员最后阅读时间
     */
    int updateLastReadTime(Long convId, Long userId);

    /**
     * 移除会话成员（软删除）
     */
    int removeMember(Long convId, Long userId);

    /**
     * 更新成员最后阅读的消息序列号
     */
    int updateLastReadSeq(Long userId, Long convId, Long seq);

    /**
     * 增加未读消息数（除指定用户外的所有成员）
     */
    int incrementUnreadCount(Long convId, Long excludeUserId);

    /**
     * 获取成员最后阅读的消息序列号
     */
    Long getLastReadSeq(Long userId, Long convId);

    /**
     * 获取成员的未读消息数
     */
    int getUnreadCount(Long convId, Long userId);

    /**
     * 重置成员未读消息数为0
     */
    int resetUnreadCount(Long convId, Long userId);

    // ============ 新增的方法 ============

    /**
     * 在conversationMember表中查找所有含有对应UserId的ConvId
     */
    List<Long> selectConvIdsByUserId(Long userId);

    /**
     * 获取对应convId, userId的用户对该conv设置的别名
     */
    String getPrivateDisplayName(Long convId, Long userId);

    /**
     * 创建一个新的单聊（处理成员相关部分）
     */
    Long createSingleConversation(Long user1Id, Long user2Id);

    /**
     * 创建群聊（处理成员相关部分）
     */
    Long createGroupConversation(Long ownerId, String convName);

    /**
     * 向对应的conv中加入userId对应的用户
     */
    int addMemberToConversation(Long convId, Long userId, String memberNickname);
}
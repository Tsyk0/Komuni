package hrc.komuni.service.impl;

import hrc.komuni.entity.ConversationMember;
import hrc.komuni.mapper.ConversationMemberMapper;
import hrc.komuni.service.ConversationMemberService;
import hrc.komuni.service.UserService;  // 需要用户服务来获取昵称
import hrc.komuni.entity.User;
import hrc.komuni.entity.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ConversationMemberServiceImpl implements ConversationMemberService {

    @Autowired
    private ConversationMemberMapper conversationMemberMapper;

    @Autowired
    private UserService userService;  // 用于获取用户信息

    // 已移除 ConversationService 依赖

    @Override
    public ConversationMember selectByConvIdAndUserId(Long convId, Long userId) {
        return conversationMemberMapper.selectByConvIdAndUserId(convId, userId);
    }

    @Override
    public List<ConversationMember> selectMembersByConvId(Long convId) {
        return conversationMemberMapper.selectMembersByConvId(convId);
    }

    @Override
    public List<ConversationMember> selectByUserId(Long userId) {
        return conversationMemberMapper.selectByUserId(userId);
    }

    @Override
    public int insertConversationMember(ConversationMember member) {
        return conversationMemberMapper.insertConversationMember(member);
    }

    @Override
    public int updateConversationMember(ConversationMember member) {
        return conversationMemberMapper.updateConversationMember(member);
    }

    @Override
    public int updatePrivateDisplayName(Long convId, Long userId, String displayName) {
        return conversationMemberMapper.updatePrivateDisplayName(convId, userId, displayName);
    }

    @Override
    public int updateLastReadTime(Long convId, Long userId) {
        return conversationMemberMapper.updateLastReadTime(convId, userId);
    }

    @Override
    public int removeMember(Long convId, Long userId) {
        return conversationMemberMapper.removeMember(convId, userId);
    }

    @Override
    public int updateLastReadSeq(Long userId, Long convId, Long seq) {
        return conversationMemberMapper.updateLastReadSeq(userId, convId, seq);
    }

    @Override
    public int incrementUnreadCount(Long convId, Long excludeUserId) {
        return conversationMemberMapper.incrementUnreadCount(convId, excludeUserId);
    }

    @Override
    public Long getLastReadSeq(Long userId, Long convId) {
        return conversationMemberMapper.getLastReadSeq(userId, convId);
    }

    @Override
    public int getUnreadCount(Long convId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectByConvIdAndUserId(convId, userId);
        return member != null ? member.getUnreadCount() : 0;
    }

    @Override
    public int resetUnreadCount(Long convId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectByConvIdAndUserId(convId, userId);
        if (member != null) {
            member.setUnreadCount(0);
            return conversationMemberMapper.updateConversationMember(member);
        }
        return 0;
    }

    // ============ 新增的方法实现 ============

    @Override
    public List<Long> selectConvIdsByUserId(Long userId) {
        return conversationMemberMapper.selectConvIdsByUserId(userId);
    }

    @Override
    public String getPrivateDisplayName(Long convId, Long userId) {
        return conversationMemberMapper.getPrivateDisplayName(convId, userId);
    }

    @Override
    @Transactional
    public Long createSingleConversation(Long user1Id, Long user2Id) {
        // 检查是否已存在单聊会话
        List<Long> user1Convs = selectConvIdsByUserId(user1Id);
        List<Long> user2Convs = selectConvIdsByUserId(user2Id);

        for (Long convId : user1Convs) {
            if (user2Convs.contains(convId)) {
                // 检查这个会话是否是单聊
                Conversation conv = conversationMemberMapper.selectConversationByConvId(convId);
                if (conv != null && conv.getConvType() == 1) {
                    return convId; // 已存在单聊会话
                }
            }
        }

        // 创建新单聊会话
        Conversation conversation = new Conversation();
        conversation.setConvType(1);
        conversation.setConvStatus(1);
        conversation.setMaxMemberCount(2);
        conversation.setCurrentMemberCount(0);
        conversation.setEnableReadReceipt(true);

        conversationMemberMapper.createSingleConversation(conversation);
        Long convId = conversation.getConvId();

        // 获取用户信息
        User user1 = userService.selectUserByUserId(user1Id);
        User user2 = userService.selectUserByUserId(user2Id);

        // 添加两个成员
        ConversationMember member1 = new ConversationMember();
        member1.setConvId(convId);
        member1.setUserId(user1Id);
        member1.setMemberRole(0);
        member1.setMemberStatus(1);
        member1.setPrivateDisplayName(user2.getUserNickname());
        member1.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(member1);

        ConversationMember member2 = new ConversationMember();
        member2.setConvId(convId);
        member2.setUserId(user2Id);
        member2.setMemberRole(0);
        member2.setMemberStatus(1);
        member2.setPrivateDisplayName(user1.getUserNickname());
        member2.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(member2);

        // 增加成员计数
        conversationMemberMapper.incrementMemberCount(convId);
        conversationMemberMapper.incrementMemberCount(convId);

        return convId;
    }

    @Override
    @Transactional
    public Long createGroupConversation(Long ownerId, String convName) {
        // 创建群聊会话
        conversationMemberMapper.createGroupConversation(convName, ownerId);

        // 注意：这里需要获取自增ID，但Mapper目前不支持直接返回ID
        // 需要修改Mapper方法使其返回ID或通过其他方式获取

        // 临时方案：查询最新创建的会话（有风险，建议修改Mapper）
        // 这里先返回一个占位值，实际需要根据你的实现调整
        Long convId = getLatestConversationId();

        // 添加群主
        ConversationMember owner = new ConversationMember();
        owner.setConvId(convId);
        owner.setUserId(ownerId);
        owner.setMemberRole(2); // 群主
        owner.setMemberStatus(1);
        owner.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(owner);

        // 增加成员计数
        conversationMemberMapper.incrementMemberCount(convId);

        return convId;
    }

    @Override
    @Transactional
    public int addMemberToConversation(Long convId, Long userId, String memberNickname) {
        ConversationMember member = new ConversationMember();
        member.setConvId(convId);
        member.setUserId(userId);
        member.setMemberNickname(memberNickname);
        member.setMemberRole(0);
        member.setMemberStatus(1);
        member.setJoinTime(new Date());

        int result = conversationMemberMapper.insertConversationMember(member);
        if (result > 0) {
            // 增加成员计数
            conversationMemberMapper.incrementMemberCount(convId);
        }
        return result;
    }

    // 辅助方法：获取最新创建的会话ID（需要改进）
    private Long getLatestConversationId() {
        // 这是一个临时方案，实际应该从Mapper获取自增ID
        // 建议修改createGroupConversation方法使其返回ID
        return null;
    }
}
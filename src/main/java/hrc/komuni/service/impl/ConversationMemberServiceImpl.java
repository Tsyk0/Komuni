package hrc.komuni.service.impl;

import hrc.komuni.entity.ConversationMember;
import hrc.komuni.mapper.ConversationMapper;
import hrc.komuni.mapper.ConversationMemberMapper;
import hrc.komuni.service.ConversationMemberService;
import hrc.komuni.service.UserService;
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
    private UserService userService;
    @Autowired
    private ConversationMapper conversationMapper;

    // 1. 查询相关方法实现
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
    public List<Long> selectConvIdsByUserId(Long userId) {
        return conversationMemberMapper.selectConvIdsByUserId(userId);
    }

    @Override
    public String getPrivateDisplayName(Long convId, Long userId) {
        return conversationMemberMapper.getPrivateDisplayName(convId, userId);
    }

    // 2. 插入相关方法实现
    @Override
    public int insertConversationMember(Long convId, Long userId) {
        return conversationMemberMapper.insertConversationMember(convId, userId);
    }

    // 3. 更新相关方法实现
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
    public int incrementUnreadCount(Long convId, Long excludeUserId) {
        return conversationMemberMapper.incrementUnreadCount(convId, excludeUserId);
    }

    @Override
    public int incrementMemberCount(Long convId) {
        return conversationMemberMapper.incrementMemberCount(convId);
    }

    @Override
    public int decrementMemberCount(Long convId) {
        return conversationMemberMapper.decrementMemberCount(convId);
    }

    // 4. 未读消息相关方法实现
    @Override
    public int getUnreadCount(Long convId, Long userId) {
        ConversationMember member = conversationMemberMapper.selectByConvIdAndUserId(convId, userId);
        return member != null ? member.getUnreadCount() : 0;
    }

    @Override
    @Transactional
    public int updateUnreadCount(Long convId, Long userId) {
        return conversationMemberMapper.updateUnreadCount(convId, userId);
    }

    @Override
    @Transactional
    public int updateAllMembersUnreadCount(Long convId) {
        return conversationMemberMapper.updateAllMembersUnreadCount(convId);
    }

    /**
     * 将未读计数设置为0（标记所有消息为已读）
     */
    public int setUnreadCountZero(Long convId, Long userId) {
        try {
            return conversationMemberMapper.setUnreadCountZero(convId, userId);
        } catch (Exception e) {
            return 0;
        }
    }



    // 5. 会话创建相关方法实现
    @Override
    @Transactional
    public Long createSingleConversation(Long user1Id, Long user2Id) {
        List<Long> user1Convs = selectConvIdsByUserId(user1Id);
        List<Long> user2Convs = selectConvIdsByUserId(user2Id);

        for (Long convId : user1Convs) {
            if (user2Convs.contains(convId)) {
                Conversation conv = conversationMapper.selectConversationByConvId(convId);
                if (conv != null && conv.getConvType() == 1) {
                    return convId;
                }
            }
        }

        Conversation conversation = new Conversation();
        conversation.setConvType(1);
        conversation.setConvStatus(1);
        conversation.setMaxMemberCount(2);
        conversation.setCurrentMemberCount(0);
        conversation.setEnableReadReceipt(true);

        conversationMemberMapper.createSingleConversation(conversation);
        Long convId = conversation.getConvId();

        User user1 = userService.selectUserByUserId(user1Id);
        User user2 = userService.selectUserByUserId(user2Id);

        ConversationMember member1 = new ConversationMember();
        member1.setConvId(convId);
        member1.setUserId(user1Id);
        member1.setMemberRole(0);
        member1.setMemberStatus(1);
        member1.setPrivateDisplayName(user2.getUserNickname());
        member1.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(convId, user1Id);

        ConversationMember member2 = new ConversationMember();
        member2.setConvId(convId);
        member2.setUserId(user2Id);
        member2.setMemberRole(0);
        member2.setMemberStatus(1);
        member2.setPrivateDisplayName(user1.getUserNickname());
        member2.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(convId, user2Id);

        conversationMemberMapper.incrementMemberCount(convId);
        conversationMemberMapper.incrementMemberCount(convId);

        return convId;
    }

    @Override
    @Transactional
    public Long createGroupConversation(Long ownerId, String convName) {
        conversationMemberMapper.createGroupConversation(convName, ownerId);
        Long convId = getLatestConversationId();

        ConversationMember owner = new ConversationMember();
        owner.setConvId(convId);
        owner.setUserId(ownerId);
        owner.setMemberRole(2);
        owner.setMemberStatus(1);
        owner.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(convId, ownerId);

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

        int result = conversationMemberMapper.insertConversationMember(convId, userId);
        if (result > 0) {
            conversationMemberMapper.incrementMemberCount(convId);
        }
        return result;
    }

    private Long getLatestConversationId() {
        return null;
    }

    private Long getLatestMessageId(Long convId) {
        return null;
    }
}
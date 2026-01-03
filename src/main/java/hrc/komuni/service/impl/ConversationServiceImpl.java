package hrc.komuni.service.impl;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.entity.User;
import hrc.komuni.mapper.ConversationMapper;
import hrc.komuni.mapper.ConversationMemberMapper;
import hrc.komuni.service.ConversationService;
import hrc.komuni.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    ConversationMemberMapper conversationMemberMapper;

    @Autowired
    UserService userService;

    @Override
    public Conversation selectConversationByConvId(Long convId) {
        return conversationMapper.selectConversationByConvId(convId);
    }

    @Override
    public List<Long> selectConvIdsByUserId(Long userId) {
        return conversationMapper.selectConvIdsByUserId(userId);
    }

    @Override
    public String getConvNameByConvId(Long convId) {
        return conversationMapper.getConvNameByConvId(convId);
    }

    @Override
    public String getPrivateDisplayName(Long convId, Long userId) {
        return conversationMapper.getPrivateDisplayName(convId, userId);
    }

    @Override
    public Integer getConvTypeByConvId(Long convId) {
        return conversationMapper.getConvTypeByConvId(convId);
    }

    @Override
    @Transactional
    public Long createSingleConversation(Long user1Id, Long user2Id) {
        // 检查是否已存在单聊会话
        List<Long> user1Convs = selectConvIdsByUserId(user1Id);
        List<Long> user2Convs = selectConvIdsByUserId(user2Id);

        for (Long convId : user1Convs) {
            if (user2Convs.contains(convId)) {
                Conversation conv = selectConversationByConvId(convId);
                if (conv != null && conv.getConvType() == 1) {
                    return convId; // 已存在单聊会话
                }
            }
        }

        // 创建新单聊会话
        Conversation conv = new Conversation();
        conv.setConvType(1); // 1-单聊
        conv.setConvStatus(1); // 正常
        conv.setMaxMemberCount(2);
        conv.setCurrentMemberCount(0);
        conv.setEnableReadReceipt(true); // 单聊默认启用已读回执
        conv.setCreateTime(new Date());

        conversationMapper.insertConversation(conv);
        Long convId = conv.getConvId();

        // 添加两个成员
        User user1 = userService.selectUserByUserId(user1Id);
        User user2 = userService.selectUserByUserId(user2Id);

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

        conversationMapper.incrementMemberCount(convId);
        conversationMapper.incrementMemberCount(convId);

        return convId;
    }

    @Override
    @Transactional
    public Long createGroupConversation(Long ownerId, String convName) {
        Conversation conv = new Conversation();
        conv.setConvType(2); // 2-群聊
        conv.setConvName(convName);
        conv.setConvOwnerId(ownerId);
        conv.setConvStatus(1);
        conv.setMaxMemberCount(500);
        conv.setCurrentMemberCount(0);
        conv.setEnableReadReceipt(true); // 群聊默认启用已读回执
        conv.setCreateTime(new Date());

        conversationMapper.insertConversation(conv);
        Long convId = conv.getConvId();

        // 添加群主
        ConversationMember owner = new ConversationMember();
        owner.setConvId(convId);
        owner.setUserId(ownerId);
        owner.setMemberRole(2); // 群主
        owner.setMemberStatus(1);
        owner.setJoinTime(new Date());
        conversationMemberMapper.insertConversationMember(owner);
        conversationMapper.incrementMemberCount(convId);

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
            conversationMapper.incrementMemberCount(convId);
        }
        return result;
    }

    @Override
    public int updatePrivateDisplayName(Long convId, Long userId, String displayName) {
        return conversationMemberMapper.updatePrivateDisplayName(convId, userId, displayName);
    }

    @Override
    public Integer getMemberCount(Long convId) {
        return conversationMapper.getMemberCount(convId);
    }

    @Override
    public List<ConversationMember> getMembersByConvId(Long convId) {
        return conversationMemberMapper.selectMembersByConvId(convId);
    }

    @Override
    public List<Conversation> getConversationsByUserId(Long userId) {
        return conversationMapper.selectConversationsByUserId(userId);
    }

    @Override
    @Transactional
    public int removeMember(Long convId, Long userId) {
        int result = conversationMemberMapper.removeMember(convId, userId);
        if (result > 0) {
            conversationMapper.decrementMemberCount(convId);
        }
        return result;
    }

    @Override
    public int updateLastReadTime(Long convId, Long userId) {
        return conversationMemberMapper.updateLastReadTime(convId, userId);
    }

    // 新增：更新群聊的已读回执设置
    @Override
    public int updateReadReceiptSetting(Long convId, Boolean enableReadReceipt) {
        // 验证是否为群聊
        Conversation conv = selectConversationByConvId(convId);
        if (conv == null || conv.getConvType() != 2) {
            throw new RuntimeException("只能设置群聊的已读回执");
        }
        return conversationMapper.updateReadReceiptSetting(convId, enableReadReceipt);
    }

    // 新增：查询群聊的已读回执设置
    @Override
    public Boolean getReadReceiptSetting(Long convId) {
        return conversationMapper.getReadReceiptSetting(convId);
    }


}
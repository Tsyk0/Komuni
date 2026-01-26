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
    public int updateLastReadTime(Long convId, Long userId) {
        return conversationMemberMapper.updateLastReadTime(convId, userId);
    }



}
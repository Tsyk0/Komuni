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
    UserService userService;

    @Override
    public Conversation selectConversationByConvId(Long convId) {
        return conversationMapper.selectConversationByConvId(convId);
    }



    @Override
    public String getConvNameByConvId(Long convId) {
        return conversationMapper.getConvNameByConvId(convId);
    }


    @Override
    public Integer getConvTypeByConvId(Long convId) {
        return conversationMapper.getConvTypeByConvId(convId);
    }


    @Override
    public Integer getMemberCount(Long convId) {
        return conversationMapper.getMemberCount(convId);
    }



    @Override
    public List<Conversation> getConversationsByUserId(Long userId) {
        return conversationMapper.selectConversationsByUserId(userId);
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
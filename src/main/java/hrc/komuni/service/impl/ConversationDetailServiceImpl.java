package hrc.komuni.service.impl;

import hrc.komuni.dto.ConversationDetailDTO;
import hrc.komuni.mapper.ConversationDetailMapper;
import hrc.komuni.service.ConversationDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationDetailServiceImpl implements ConversationDetailService {

    @Autowired
    private ConversationDetailMapper conversationDetailMapper;

    @Override
    @Cacheable(cacheNames = "conversationDetailsByUserId", key = "#userId", unless = "#result == null")
    public List<ConversationDetailDTO> getConversationDetailsByUserId(Long userId) {
        try {
            // 获取基础会话信息
            List<ConversationDetailDTO> conversations = conversationDetailMapper.selectBasicConversationsByUserId(userId);

            // 为每个会话获取最后一条消息
            for (ConversationDetailDTO conversation : conversations) {
                ConversationDetailDTO.LastMessageInfo lastMessage =
                        conversationDetailMapper.selectLastMessageByConvId(conversation.getConvId());
                conversation.setLastMessage(lastMessage);
            }

            return conversations;
        } catch (Exception e) {
            System.err.println("获取会话详情失败: " + e.getMessage());
            throw new RuntimeException("获取会话详情失败", e);
        }
    }
}
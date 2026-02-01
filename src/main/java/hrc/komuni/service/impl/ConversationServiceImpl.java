package hrc.komuni.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<Conversation> selectConversationsBatch(List<Long> convIds) {
        if (CollectionUtils.isEmpty(convIds)) {
            return Collections.emptyList();
        }

        try {
            // 去重并过滤无效ID
            List<Long> validIds = convIds.stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .collect(Collectors.toList());

            if (validIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 使用新的批量查询方法
            List<Conversation> conversations = conversationMapper.selectConversationsBatch(validIds);

            // 记录未找到的会话ID（用于调试）
            if (conversations.size() < validIds.size()) {
                Set<Long> foundIds = conversations.stream()
                        .map(Conversation::getConvId)
                        .collect(Collectors.toSet());

                List<Long> notFoundIds = validIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());

                System.out.println("以下会话ID未找到: " + notFoundIds);
            }

            return conversations;
        } catch (Exception e) {
            System.err.println("批量查询会话失败: " + e.getMessage());
            throw new RuntimeException("批量查询会话失败", e);
        }
    }



    @Override
    public String getConvNameByConvId(Long convId) {
        return conversationMapper.getConvNameByConvId(convId);
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

    @Override
    public String updateConversationAttriUserOrientedByConvId(Conversation conversation) {
        if (conversation == null || conversation.getConvId() == null) {
            return "会话ID不能为空";
        }
        Conversation stored = selectConversationByConvId(conversation.getConvId());
        if (stored == null) {
            return "会话不存在";
        }
        int rows = conversationMapper.updateConversationAttriUserOrientedByConvId(conversation);
        return rows == 1 ? "更新成功" : "更新失败";
    }

}
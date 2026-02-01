package hrc.komuni.service;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import java.util.List;

public interface ConversationService {
    Conversation selectConversationByConvId(Long convId);

    String getConvNameByConvId(Long convId);


    /**
     * 批量查询会话信息
     */
    List<Conversation> selectConversationsBatch(List<Long> convIds);

    Integer getMemberCount(Long convId);


    List<Conversation> getConversationsByUserId(Long userId);

    // 新增：更新群聊的已读回执设置
    int updateReadReceiptSetting(Long convId, Boolean enableReadReceipt);

    // 新增：查询群聊的已读回执设置
    Boolean getReadReceiptSetting(Long convId);

    /**
     * 更新会话的用户侧可修改属性（不包含 max_member_count、current_member_count、
     * conv_status、create_time、update_time、conv_owner_id）
     */
    String updateConversationAttriUserOrientedByConvId(Conversation conversation);

}
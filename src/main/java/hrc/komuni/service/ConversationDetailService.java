package hrc.komuni.service;

import hrc.komuni.dto.ConversationDetailDTO;
import java.util.List;

public interface ConversationDetailService {
    /**
     * 获取用户的会话详情列表
     * 包含会话信息、最后一条消息和显示信息
     */
    List<ConversationDetailDTO> getConversationDetailsByUserId(Long userId);
}
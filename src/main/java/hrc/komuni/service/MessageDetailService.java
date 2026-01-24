package hrc.komuni.service;

import hrc.komuni.dto.MessageDetailDTO;

import java.util.List;

public interface MessageDetailService {
    /**
     * 获取消息详情列表（包含发送者信息）
     */
    List<MessageDetailDTO> getMessageDetailsByConvId(Long convId, Integer page, Integer pageSize, Long currentUserId);
}

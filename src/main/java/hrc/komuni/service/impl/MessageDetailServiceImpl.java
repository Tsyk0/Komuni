package hrc.komuni.service.impl;

import hrc.komuni.dto.MessageDetailDTO;
import hrc.komuni.service.MessageDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import hrc.komuni.mapper.MessageDetailMapper;

@Service
public class MessageDetailServiceImpl implements MessageDetailService {

    @Autowired
    private MessageDetailMapper messageDetailMapper;  // 变量名改为小写，符合Java规范

    @Override
    public List<MessageDetailDTO> getMessageDetailsByConvId(Long convId, Integer page, Integer pageSize, Long currentUserId) {
        try {
            Integer offset = (page - 1) * pageSize;

            // 直接从Mapper获取数据，SQL中已经计算了displayName和isSentByMe
            List<MessageDetailDTO> dtos = messageDetailMapper.selectMessageDetailsByConvId(
                    convId, currentUserId, offset, pageSize
            );

            // 不再需要处理displayName和isSentByMe，因为SQL中已经计算好了
            // 但可以添加一些后处理逻辑（如果需要）

            // 确保displayName不为空
            for (MessageDetailDTO dto : dtos) {
                if (dto.getDisplayName() == null || dto.getDisplayName().trim().isEmpty()) {
                    // 如果SQL计算失败，这里设置一个默认值
                    dto.setDisplayName(calculateFallbackDisplayName(dto, currentUserId));
                }

                // 确保isSentByMe不为空
                if (dto.getIsSentByMe() == null) {
                    boolean isSentByMe = currentUserId != null && currentUserId.equals(dto.getSenderId());
                    dto.setIsSentByMe(isSentByMe);
                }
            }

            return dtos;
        } catch (Exception e) {
            System.err.println("获取消息详情失败: " + e.getMessage());
            throw new RuntimeException("获取消息详情失败", e);
        }
    }

    /**
     * 备用的显示名称计算（当SQL计算失败时使用）
     */
    private String calculateFallbackDisplayName(MessageDetailDTO dto, Long currentUserId) {
        // 如果发送者是自己
        boolean isSentByMe = currentUserId != null && currentUserId.equals(dto.getSenderId());
        if (isSentByMe) {
            // 自己发送的消息显示自己的昵称（需要查询数据库）
            // 这里简化处理，实际应该从数据库查询或缓存获取
            return "我";
        }

        // 优先使用群昵称
        if (dto.getMemberNickname() != null && !dto.getMemberNickname().trim().isEmpty()) {
            return dto.getMemberNickname();
        }

        // 单聊时使用私有显示名称
        if (dto.getConvType() != null && dto.getConvType() == 1) {
            if (dto.getPrivateDisplayName() != null && !dto.getPrivateDisplayName().trim().isEmpty()) {
                return dto.getPrivateDisplayName();
            }
        }

        // 默认显示用户ID
        return "用户" + dto.getSenderId();
    }

    // 删除原有的calculateDisplayName方法
}
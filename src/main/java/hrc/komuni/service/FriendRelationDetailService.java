package hrc.komuni.service;

import hrc.komuni.dto.FriendRelationDetailDTO;
import java.util.List;

public interface FriendRelationDetailService {

    /**
     * 获取用户的好友列表（包含好友详细信息）
     * @param userId 用户ID
     * @return 好友列表详情
     */
    List<FriendRelationDetailDTO> getFriendListbyUserId(Long userId);
}
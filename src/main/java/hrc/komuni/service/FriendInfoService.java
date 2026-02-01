package hrc.komuni.service;
import hrc.komuni.dto.FriendInfoDTO;

public interface FriendInfoService {

    /**
     * 根据userId和friendId获取好友关系与好友信息
     *
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 好友详情
     */
    FriendInfoDTO getFriendInfoByUserIdAndFriendId(Long userId, Long friendId);
}

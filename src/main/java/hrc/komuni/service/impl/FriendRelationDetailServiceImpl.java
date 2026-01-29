package hrc.komuni.service.impl;

import hrc.komuni.dto.FriendRelationDetailDTO;
import hrc.komuni.mapper.FriendRelationDetailMapper;
import hrc.komuni.service.FriendRelationDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRelationDetailServiceImpl implements FriendRelationDetailService {

    private final FriendRelationDetailMapper friendRelationDetailMapper;

    @Override
    @Cacheable(cacheNames = "friendListByUserId", key = "#userId", unless = "#result == null")
    public List<FriendRelationDetailDTO> getFriendListbyUserId(Long userId) {
        log.info("获取用户{}的好友列表", userId);

        List<FriendRelationDetailDTO> friends = friendRelationDetailMapper.getFriendListByUserId(userId);

        // 处理显示名称：如果有备注名，优先显示备注名
        if (friends != null) {
            friends.forEach(friend -> {
                if (friend.getRemarkName() != null && !friend.getRemarkName().trim().isEmpty()) {
                    friend.setFriendNickname(friend.getRemarkName());
                }
            });
        }

        return friends;
    }
}
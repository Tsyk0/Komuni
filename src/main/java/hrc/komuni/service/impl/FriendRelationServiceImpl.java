package hrc.komuni.service.impl;

import hrc.komuni.entity.FriendRelation;
import hrc.komuni.entity.User;
import hrc.komuni.mapper.FriendRelationMapper;
import hrc.komuni.service.FriendRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FriendRelationServiceImpl implements FriendRelationService {
    @Autowired
    FriendRelationMapper friendRelationMapper;

    @Override
    public List<String> selectAllFriendsNameByUserId(Long userId) {
        return friendRelationMapper.selectAllFriendsNameByUserId(userId);
    }

    @Override
    public List<User> selectAllFriendsByUserId(Long userId) {
        return friendRelationMapper.selectAllFriendsByUserId(userId);
    }

    @Override
    public FriendRelation selectByUserIdAndFriendId(Long userId, Long friendId) {
        return friendRelationMapper.selectByUserIdAndFriendId(userId, friendId);
    }

    @Override
    @Transactional
    public int addFriend(Long userId, Long friendId, String addSource) {
        // 检查是否已存在
        FriendRelation existing = selectByUserIdAndFriendId(userId, friendId);
        if (existing != null) {
            if (existing.getRelationStatus() == 1) {
                return 0; // 已是好友
            }
            // 恢复好友关系
            return friendRelationMapper.updateFriendRelation(
                    userId, friendId, 1, null, null);
        }

        // 创建双向好友关系
        FriendRelation relation1 = new FriendRelation();
        relation1.setUserId(userId);
        relation1.setFriendId(friendId);
        relation1.setRelationStatus(1);
        relation1.setAddSource(addSource);
        relation1.setAddTime(new Date());
        friendRelationMapper.insertFriendRelation(relation1);

        FriendRelation relation2 = new FriendRelation();
        relation2.setUserId(friendId);
        relation2.setFriendId(userId);
        relation2.setRelationStatus(1);
        relation2.setAddSource(addSource);
        relation2.setAddTime(new Date());
        friendRelationMapper.insertFriendRelation(relation2);

        return 1;
    }

    @Override
    @Transactional
    public int removeFriend(Long userId, Long friendId) {
        friendRelationMapper.removeFriendRelation(userId, friendId);
        friendRelationMapper.removeFriendRelation(friendId, userId);
        return 1;
    }

    @Override
    public int updateFriendRelation(Long userId, Long friendId, String remarkName, String friendGroup) {
        return friendRelationMapper.updateFriendRelation(userId, friendId, 1, remarkName, friendGroup);
    }

    @Override
    public int blockFriend(Long userId, Long friendId) {
        return friendRelationMapper.updateFriendRelation(userId, friendId, 2, null, null);
    }
}
package hrc.komuni.service;

import hrc.komuni.entity.FriendRelation;
import hrc.komuni.entity.User;
import java.util.List;

public interface FriendRelationService {
    List<String> selectAllFriendsNameByUserId(Long userId);

    List<User> selectAllFriendsByUserId(Long userId);

    FriendRelation selectByUserIdAndFriendId(Long userId, Long friendId);

    int addFriend(Long userId, Long friendId, String addSource);

    int removeFriend(Long userId, Long friendId);

    int updateFriendRelation(Long userId, Long friendId, String remarkName, String friendGroup);

    int blockFriend(Long userId, Long friendId);
}
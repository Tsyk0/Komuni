package hrc.komuni.service.impl;
import hrc.komuni.dto.FriendInfoDTO;
import hrc.komuni.mapper.FriendInfoMapper;
import hrc.komuni.service.FriendInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendInfoServiceImpl implements FriendInfoService {

   @Autowired
   private FriendInfoMapper FriendInfoMapper;

    @Override
    public FriendInfoDTO getFriendInfoByUserIdAndFriendId(Long userId, Long friendId) {
        return FriendInfoMapper.getFriendInfoByUserIdAndFriendId(userId, friendId);
    }
}

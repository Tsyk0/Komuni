package hrc.komuni.controller;

import hrc.komuni.dto.FriendInfoDTO;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FriendInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(originPatterns = "http://localhost:3000")
@RestController
@RequestMapping("/friendInfo")
@RequiredArgsConstructor
public class FriendDetailController {

    @Autowired
    private FriendInfoService friendInfoService;

    /**
     * 获取好友关系详情（包含好友用户信息）
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 好友关系及好友信息
     */
    @GetMapping("/getFriendInfoByUserIdAndFriendId")
    public ApiResponse<FriendInfoDTO> getFriendDetailByUserIdAndFriendId(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long friendId) {
        try {
            FriendInfoDTO info = friendInfoService.getFriendInfoByUserIdAndFriendId(userId, friendId);
            if (info == null) {
                return ApiResponse.notFound("好友关系不存在");
            }
            return ApiResponse.success("查询成功", info);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友详情失败: " + e.getMessage());
        }
    }
}

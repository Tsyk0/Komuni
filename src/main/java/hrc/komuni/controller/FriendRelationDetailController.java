package hrc.komuni.controller;

import hrc.komuni.dto.FriendRelationDetailDTO;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FriendRelationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/friendRelationDetail")
@RequiredArgsConstructor
public class FriendRelationDetailController {

    private final FriendRelationDetailService friendRelationDetailService;

    /**
     * 获取用户好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    @GetMapping("/getFriendListbyUserId")
    public ApiResponse<List<FriendRelationDetailDTO>> getFriendListbyUserId(
            @RequestAttribute("userId") Long userId) {
        try {
            List<FriendRelationDetailDTO> friends = friendRelationDetailService.getFriendListbyUserId(userId);
            return ApiResponse.success("查询成功", friends);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友列表失败: " + e.getMessage());
        }
    }
}
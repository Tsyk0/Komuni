package hrc.komuni.controller;

import hrc.komuni.entity.FriendRelation;
import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FriendRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/friend")
public class FriendRelationController {
    @Autowired
    FriendRelationService friendRelationService;

    @GetMapping("/selectAllFriendsNameByUserId")
    public ApiResponse<List<String>> selectAllFriendsNameByUserId(@RequestParam Long userId) {
        try {
            List<String> friendNames = friendRelationService.selectAllFriendsNameByUserId(userId);
            return ApiResponse.success("查询成功", friendNames);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友名称失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectAllFriendsByUserId")
    public ApiResponse<List<User>> selectAllFriendsByUserId(@RequestParam Long userId) {
        try {
            List<User> friends = friendRelationService.selectAllFriendsByUserId(userId);
            return ApiResponse.success("查询成功", friends);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友失败: " + e.getMessage());
        }
    }

    @PostMapping("/addFriend")
    public ApiResponse<String> addFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId,
            @RequestParam(required = false) String addSource) {
        try {
            int result = friendRelationService.addFriend(userId, friendId, addSource);
            if (result > 0) {
                return ApiResponse.success("添加好友成功");
            } else {
                return ApiResponse.badRequest("添加好友失败或已是好友");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("添加好友失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/removeFriend")
    public ApiResponse<String> removeFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        try {
            int result = friendRelationService.removeFriend(userId, friendId);
            if (result > 0) {
                return ApiResponse.success("删除好友成功");
            } else {
                return ApiResponse.badRequest("删除好友失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("删除好友失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateFriendRelation")
    public ApiResponse<String> updateFriendRelation(
            @RequestParam Long userId,
            @RequestParam Long friendId,
            @RequestParam(required = false) String remarkName,
            @RequestParam(required = false) String friendGroup) {
        try {
            int result = friendRelationService.updateFriendRelation(userId, friendId, remarkName, friendGroup);
            if (result > 0) {
                return ApiResponse.success("更新好友信息成功");
            } else {
                return ApiResponse.badRequest("更新好友信息失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("更新好友信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/blockFriend")
    public ApiResponse<String> blockFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        try {
            int result = friendRelationService.blockFriend(userId, friendId);
            if (result > 0) {
                return ApiResponse.success("拉黑好友成功");
            } else {
                return ApiResponse.badRequest("拉黑好友失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("拉黑好友失败: " + e.getMessage());
        }
    }
}
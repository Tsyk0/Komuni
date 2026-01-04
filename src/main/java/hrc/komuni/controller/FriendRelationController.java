package hrc.komuni.controller;

import hrc.komuni.entity.FriendRelation;
import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.FriendRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/friend")
@Tag(name = "好友管理", description = "好友关系的增删改查操作接口")
public class FriendRelationController {
    @Autowired
    FriendRelationService friendRelationService;

    @GetMapping("/selectAllFriendsNameByUserId")
    @Operation(summary = "查询好友名称列表", description = "根据用户ID查询该用户所有好友的名称列表")
    public ApiResponse<List<String>> selectAllFriendsNameByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<String> friendNames = friendRelationService.selectAllFriendsNameByUserId(userId);
            return ApiResponse.success("查询成功", friendNames);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友名称失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectAllFriendsByUserId")
    @Operation(summary = "查询好友详细信息", description = "根据用户ID查询该用户所有好友的详细信息")
    public ApiResponse<List<User>> selectAllFriendsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<User> friends = friendRelationService.selectAllFriendsByUserId(userId);
            return ApiResponse.success("查询成功", friends);
        } catch (Exception e) {
            return ApiResponse.serverError("查询好友失败: " + e.getMessage());
        }
    }

    @PostMapping("/addFriend")
    @Operation(summary = "添加好友", description = "添加好友关系，双向建立好友关系")
    public ApiResponse<String> addFriend(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "好友ID", required = true) @RequestParam Long friendId,
            @Parameter(description = "添加来源", required = false) @RequestParam(required = false) String addSource) {
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
    @Operation(summary = "删除好友", description = "删除好友关系，双向删除")
    public ApiResponse<String> removeFriend(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "好友ID", required = true) @RequestParam Long friendId) {
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
    @Operation(summary = "更新好友信息", description = "更新好友的备注名和分组信息")
    public ApiResponse<String> updateFriendRelation(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "好友ID", required = true) @RequestParam Long friendId,
            @Parameter(description = "备注名", required = false) @RequestParam(required = false) String remarkName,
            @Parameter(description = "好友分组", required = false) @RequestParam(required = false) String friendGroup) {
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
    @Operation(summary = "拉黑好友", description = "将好友拉入黑名单，暂时或永久屏蔽")
    public ApiResponse<String> blockFriend(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "好友ID", required = true) @RequestParam Long friendId) {
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
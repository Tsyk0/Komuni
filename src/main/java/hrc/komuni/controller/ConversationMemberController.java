package hrc.komuni.controller;

import hrc.komuni.entity.ConversationMember;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/conversationMember")
@Tag(name = "会话成员管理", description = "会话成员相关的操作接口")
public class ConversationMemberController {

    @Autowired
    private ConversationMemberService conversationMemberService;

    @GetMapping("/getByConvAndUser")
    @Operation(summary = "查询会话成员", description = "根据会话ID和用户ID查询会话成员信息")
    public ApiResponse<ConversationMember> getByConvAndUser(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            ConversationMember member = conversationMemberService.selectByConvIdAndUserId(convId, userId);
            if (member == null) {
                return ApiResponse.notFound("会话成员不存在");
            }
            return ApiResponse.success("查询成功", member);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/listByConv")
    @Operation(summary = "查询会话所有成员", description = "根据会话ID查询该会话的所有成员")
    public ApiResponse<List<ConversationMember>> listByConv(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/listByUser")
    @Operation(summary = "查询用户所在的所有会话", description = "根据用户ID查询该用户参与的所有会话成员关系")
    public ApiResponse<List<ConversationMember>> listByUser(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectByUserId(userId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "添加会话成员", description = "添加用户到指定会话")
    public ApiResponse<Integer> addMember(@RequestBody ConversationMember member) {
        try {
            int result = conversationMemberService.insertConversationMember(member);
            if (result > 0) {
                return ApiResponse.success("添加成功", result);
            } else {
                return ApiResponse.badRequest("添加失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("添加失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新会话成员信息", description = "更新会话成员的信息（如昵称、角色等）")
    public ApiResponse<Integer> updateMember(@RequestBody ConversationMember member) {
        try {
            int result = conversationMemberService.updateConversationMember(member);
            if (result > 0) {
                return ApiResponse.success("更新成功", result);
            } else {
                return ApiResponse.badRequest("更新失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/updatePrivateName")
    @Operation(summary = "更新单聊显示名称", description = "在单聊中更新对方的显示名称")
    public ApiResponse<Integer> updatePrivateDisplayName(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "显示名称", required = true) @RequestParam String displayName) {
        try {
            int result = conversationMemberService.updatePrivateDisplayName(convId, userId, displayName);
            if (result > 0) {
                return ApiResponse.success("更新成功", result);
            } else {
                return ApiResponse.badRequest("更新失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateLastReadTime")
    @Operation(summary = "更新最后阅读时间", description = "更新成员的最后消息阅读时间")
    public ApiResponse<Integer> updateLastReadTime(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.updateLastReadTime(convId, userId);
            if (result > 0) {
                return ApiResponse.success("更新成功", result);
            } else {
                return ApiResponse.badRequest("更新失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/remove")
    @Operation(summary = "移除会话成员", description = "将用户从会话中移除（软删除）")
    public ApiResponse<Integer> removeMember(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.removeMember(convId, userId);
            if (result > 0) {
                return ApiResponse.success("移除成功", result);
            } else {
                return ApiResponse.badRequest("移除失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("移除失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateLastReadSeq")
    @Operation(summary = "更新最后阅读序列号", description = "更新成员最后阅读的消息序列号")
    public ApiResponse<Integer> updateLastReadSeq(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "消息序列号", required = true) @RequestParam Long seq) {
        try {
            int result = conversationMemberService.updateLastReadSeq(userId, convId, seq);
            if (result > 0) {
                return ApiResponse.success("更新成功", result);
            } else {
                return ApiResponse.badRequest("更新失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/getLastReadSeq")
    @Operation(summary = "获取最后阅读序列号", description = "获取成员最后阅读的消息序列号")
    public ApiResponse<Long> getLastReadSeq(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Long seq = conversationMemberService.getLastReadSeq(userId, convId);
            return ApiResponse.success("查询成功", seq);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/getUnreadCount")
    @Operation(summary = "获取未读消息数", description = "获取用户在会话中的未读消息数")
    public ApiResponse<Integer> getUnreadCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int count = conversationMemberService.getUnreadCount(convId, userId);
            return ApiResponse.success("查询成功", count);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/resetUnreadCount")
    @Operation(summary = "重置未读消息数", description = "将用户的未读消息数重置为0")
    public ApiResponse<Integer> resetUnreadCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.resetUnreadCount(convId, userId);
            if (result > 0) {
                return ApiResponse.success("重置成功", result);
            } else {
                return ApiResponse.badRequest("重置失败，成员不存在");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("重置失败: " + e.getMessage());
        }
    }


}
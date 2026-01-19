package hrc.komuni.controller;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.entity.User;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationMemberService;
import hrc.komuni.service.ConversationService;
import hrc.komuni.service.UserService;
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
    @Autowired
    private UserService userService;
    @Autowired
    private ConversationService conversationService;

    // 1. 查询相关接口
    @GetMapping("/selectByConvIdAndUserId")
    @Operation(summary = "查询会话成员", description = "根据会话ID和用户ID查询会话成员信息")
    public ApiResponse<ConversationMember> selectByConvIdAndUserId(
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

    @GetMapping("/selectMembersByConvId")
    @Operation(summary = "查询会话所有状态正常的成员", description = "根据会话ID查询该会话的所有成员")
    public ApiResponse<List<ConversationMember>> selectMembersByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/selectByUserId")
    @Operation(summary = "查询用户所在的所有会话", description = "根据用户ID查询该用户参与的所有会话成员关联，包含单聊")
    public ApiResponse<List<ConversationMember>> selectByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectByUserId(userId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
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

    @GetMapping("/getPrivateDisplayName")
    @Operation(summary = "获取私聊显示名称", description = "获取用户在单聊中对对方的显示名称")
    public ApiResponse<String> getPrivateDisplayName(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            String displayName = conversationMemberService.getPrivateDisplayName(convId, userId);
            if (displayName == null) {
                return ApiResponse.notFound("未设置显示名称");
            }
            return ApiResponse.success("查询成功", displayName);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

//   添加set方法

    @GetMapping("/selectConvIdsByUserId")
    @Operation(summary = "获取用户参与的会话ID列表", description = "根据用户ID查询该用户参与的所有会话ID")
    public ApiResponse<List<Long>> selectConvIdsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Long> convIds = conversationMemberService.selectConvIdsByUserId(userId);
            return ApiResponse.success("查询成功", convIds);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/getUnreadCount")
    @Operation(summary = "获取未读消息数", description = "获取用户在会话中的未读消息数，但仍然未实现与conversation的currentmsgseq的联动（每当收到新消息就触发一遍计算未读方法），等待后续补充")
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

    @PostMapping("/setUnreadCountZero")
    @Operation(summary = "标记所有消息为已读", description = "将指定会话的未读消息数设置为0")
    public ApiResponse<Integer> setUnreadCountZero(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.setUnreadCountZero(convId, userId);
            System.out.println("标记为已读: convId=" + convId + ", userId=" + userId + ", 结果=" + result);
            return ApiResponse.success("标记所有消息为已读成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("标记为已读失败: " + e.getMessage());
        }
    }

    // 2. 插入相关接口
    @PostMapping("/insertConversationMember")
    @Operation(summary = "添加会话成员", description = "添加用户到会话，只设置必要字段")
    public ApiResponse<Integer> insertConversationMember(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "要添加的用户ID", required = true) @RequestParam Long userId) {
        try {
            User user = userService.selectUserByUserId(userId);
            if (user == null) {
                return ApiResponse.badRequest("用户不存在");
            }

            Conversation conv = conversationService.selectConversationByConvId(convId);
            if (conv == null) {
                return ApiResponse.badRequest("会话不存在");
            }

            ConversationMember existingMember = conversationMemberService.selectByConvIdAndUserId(convId, userId);
            if (existingMember != null) {
                return ApiResponse.badRequest("用户已是会话成员");
            }

            int result = conversationMemberService.insertConversationMember(convId, userId);

            if (result > 0) {
                conversationMemberService.incrementMemberCount(convId);
                return ApiResponse.success("添加成功", result);
            } else {
                return ApiResponse.badRequest("添加失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("添加失败: " + e.getMessage());
        }
    }

    // 3. 更新相关接口
    @PostMapping("/updateConversationMember")
    @Operation(summary = "更新会话成员信息", description = "更新会话成员的信息（如昵称、角色等）")
    public ApiResponse<Integer> updateConversationMember(@RequestBody ConversationMember member) {
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

    @PostMapping("/updatePrivateDisplayName")
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

    @PostMapping("/removeMember")
    @Operation(summary = "移除会话成员", description = "将用户从会话中移除（软删除）")
    public ApiResponse<Integer> removeMember(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.removeMember(convId, userId);
            if (result > 0) {
                // 减少成员计数
                conversationMemberService.decrementMemberCount(convId);
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

    @PostMapping("/incrementUnreadCount")
    @Operation(summary = "增加未读消息数", description = "给除了指定用户外的所有会话成员增加未读消息数")
    public ApiResponse<Integer> incrementUnreadCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "排除的用户ID", required = true) @RequestParam Long excludeUserId) {
        try {
            int result = conversationMemberService.incrementUnreadCount(convId, excludeUserId);
            return ApiResponse.success("操作成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("操作失败: " + e.getMessage());
        }
    }



    @PostMapping("/updateUnreadCount")
    @Operation(summary = "基于最后阅读更新未读数", description = "根据最后阅读的消息更新未读消息数")
    public ApiResponse<Integer> updateUnreadCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.updateUnreadCount(convId, userId);
            System.out.println(convId+"and"+userId+"updateUNREAD");
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateAllMembersUnreadCount")
    @Operation(summary = "更新所有成员未读数", description = "更新会话中所有成员的未读消息数")
    public ApiResponse<Integer> updateAllMembersUnreadCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            int result = conversationMemberService.updateAllMembersUnreadCount(convId);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            return ApiResponse.serverError("更新失败: " + e.getMessage());
        }
    }

    // 4. 会话创建相关接口
    @PostMapping("/createSingleConversation")
    @Operation(summary = "创建单聊会话（成员管理）", description = "创建单聊会话并添加双方成员")
    public ApiResponse<Long> createSingleConversation(
            @Parameter(description = "用户1ID", required = true) @RequestParam Long user1Id,
            @Parameter(description = "用户2ID", required = true) @RequestParam Long user2Id) {
        try {
            Long convId = conversationMemberService.createSingleConversation(user1Id, user2Id);
            return ApiResponse.success("创建单聊会话成功", convId);
        } catch (Exception e) {
            return ApiResponse.serverError("创建单聊会话失败: " + e.getMessage());
        }
    }

    @PostMapping("/createGroupConversation")
    @Operation(summary = "创建群聊会话（成员管理）", description = "创建群聊会话并添加群主")
    public ApiResponse<Long> createGroupConversation(
            @Parameter(description = "群主ID", required = true) @RequestParam Long ownerId,
            @Parameter(description = "群聊名称", required = true) @RequestParam String convName) {
        try {
            Long convId = conversationMemberService.createGroupConversation(ownerId, convName);
            return ApiResponse.success("创建群聊会话成功", convId);
        } catch (Exception e) {
            return ApiResponse.serverError("创建群聊会话失败: " + e.getMessage());
        }
    }

    @PostMapping("/addMemberToConversation")
    @Operation(summary = "添加成员到会话", description = "将用户添加到指定的会话中")
    public ApiResponse<String> addMemberToConversation(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "成员昵称", required = false) @RequestParam(required = false) String memberNickname) {
        try {
            int result = conversationMemberService.addMemberToConversation(convId, userId, memberNickname);
            if (result > 0) {
                return ApiResponse.success("添加成员成功");
            } else {
                return ApiResponse.badRequest("添加成员失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("添加成员失败: " + e.getMessage());
        }
    }


}
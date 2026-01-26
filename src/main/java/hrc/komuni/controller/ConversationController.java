package hrc.komuni.controller;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationService;
import hrc.komuni.service.ConversationMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/conversation")
@Tag(name = "会话管理", description = "会话相关的操作接口")
public class ConversationController {
    @Autowired
    ConversationService conversationService;

    @Autowired
    ConversationMemberService conversationMemberService;

    @GetMapping("/selectConversationByConvId")
    @Operation(summary = "查询会话信息", description = "根据会话ID查询会话详细信息")
    public ApiResponse<Conversation> selectConversationByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Conversation conv = conversationService.selectConversationByConvId(convId);
            if (conv == null) {
                return ApiResponse.notFound("会话不存在");
            }
            return ApiResponse.success("查询成功", conv);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话失败: " + e.getMessage());
        }
    }



    @GetMapping("/selectConvIdsByUserId")
    @Operation(summary = "查询用户参与的会话ID", description = "根据用户ID查询该用户参与的所有会话ID列表")
    public ApiResponse<List<Long>> selectConvIdsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Long> convIds = conversationMemberService.selectConvIdsByUserId(userId);
            return ApiResponse.success("查询成功", convIds);
        } catch (Exception e) {
            return ApiResponse.serverError("查询用户会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/getConversationsByUserId")
    @Operation(summary = "查询用户的会话列表", description = "根据用户ID查询该用户参与的所有会话详情列表")
    public ApiResponse<List<Conversation>> getConversationsByUserId(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
            return ApiResponse.success("查询成功", conversations);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/getConvName")
    @Operation(summary = "获取会话显示名称", description = "获取会话的显示名称，单聊时返回对方昵称，群聊时返回群聊名称")
    public ApiResponse<String> getConvName(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            String privateName = conversationMemberService.getPrivateDisplayName(convId, userId);
            if (privateName != null) {
                return ApiResponse.success("查询成功", privateName);
            }
            String convName = conversationService.getConvNameByConvId(convId);
            return ApiResponse.success("查询成功", convName);
        } catch (Exception e) {
            return ApiResponse.serverError("获取会话名称失败: " + e.getMessage());
        }
    }

    @PostMapping("/createSingleConversation")
    @Operation(summary = "创建单聊会话", description = "创建两个用户之间的单聊会话，如果已存在则返回现有会话")
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
    @Operation(summary = "创建群聊会话", description = "创建新的群聊会话，创建者自动成为群主")
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

    @GetMapping("/selectMembersByConvId")
    @Operation(summary = "查询会话成员列表", description = "根据会话ID查询该会话的所有成员信息")
    public ApiResponse<List<ConversationMember>> selectMembersByConvId(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            List<ConversationMember> members = conversationMemberService.selectMembersByConvId(convId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询成员失败: " + e.getMessage());
        }
    }

    @GetMapping("/getMemberCount")
    @Operation(summary = "查询会话成员数量", description = "获取指定会话的成员总数")
    public ApiResponse<Integer> getMemberCount(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Integer count = conversationService.getMemberCount(convId);
            return ApiResponse.success("查询成功", count);
        } catch (Exception e) {
            return ApiResponse.serverError("查询成员数量失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/removeMember")
    @Operation(summary = "移除会话成员", description = "将用户从指定的会话中移除")
    public ApiResponse<String> removeMember(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int result = conversationMemberService.removeMember(convId, userId);
            if (result > 0) {
                return ApiResponse.success("移除成员成功");
            } else {
                return ApiResponse.badRequest("移除成员失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("移除成员失败: " + e.getMessage());
        }
    }

    @PostMapping("/updateReadReceiptSetting")
    @Operation(summary = "更新已读回执设置", description = "更新群聊的消息已读回执设置（仅群聊可用）")
    public ApiResponse<String> updateReadReceiptSetting(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId,
            @Parameter(description = "是否启用已读回执", required = true) @RequestParam Boolean enableReadReceipt) {
        try {
            int result = conversationService.updateReadReceiptSetting(convId, enableReadReceipt);
            if (result > 0) {
                return ApiResponse.success("更新设置成功");
            } else {
                return ApiResponse.badRequest("更新设置失败");
            }
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.serverError("更新设置失败: " + e.getMessage());
        }
    }

    @GetMapping("/getReadReceiptSetting")
    @Operation(summary = "查询已读回执设置", description = "查询群聊的消息已读回执设置状态")
    public ApiResponse<Boolean> getReadReceiptSetting(
            @Parameter(description = "会话ID", required = true) @RequestParam Long convId) {
        try {
            Boolean setting = conversationService.getReadReceiptSetting(convId);
            return ApiResponse.success("查询成功", setting);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }
}
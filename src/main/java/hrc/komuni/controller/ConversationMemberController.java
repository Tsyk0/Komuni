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



}
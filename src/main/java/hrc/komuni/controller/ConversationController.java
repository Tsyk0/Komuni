package hrc.komuni.controller;

import hrc.komuni.entity.Conversation;
import hrc.komuni.entity.ConversationMember;
import hrc.komuni.response.ApiResponse;
import hrc.komuni.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/conversation")
public class ConversationController {
    @Autowired
    ConversationService conversationService;

    // ... 保留所有现有方法 ...

    @GetMapping("/selectByConvId")
    public ApiResponse<Conversation> selectByConvId(@RequestParam Long convId) {
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
    public ApiResponse<List<Long>> selectConvIdsByUserId(@RequestParam Long userId) {
        try {
            List<Long> convIds = conversationService.selectConvIdsByUserId(userId);
            return ApiResponse.success("查询成功", convIds);
        } catch (Exception e) {
            return ApiResponse.serverError("查询用户会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/getConversationsByUserId")
    public ApiResponse<List<Conversation>> getConversationsByUserId(@RequestParam Long userId) {
        try {
            List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
            return ApiResponse.success("查询成功", conversations);
        } catch (Exception e) {
            return ApiResponse.serverError("查询会话列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/getConvName")
    public ApiResponse<String> getConvName(
            @RequestParam Long convId,
            @RequestParam Long userId) {
        try {
            String privateName = conversationService.getPrivateDisplayName(convId, userId);
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
    public ApiResponse<Long> createSingleConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        try {
            Long convId = conversationService.createSingleConversation(user1Id, user2Id);
            return ApiResponse.success("创建单聊会话成功", convId);
        } catch (Exception e) {
            return ApiResponse.serverError("创建单聊会话失败: " + e.getMessage());
        }
    }

    @PostMapping("/createGroupConversation")
    public ApiResponse<Long> createGroupConversation(
            @RequestParam Long ownerId,
            @RequestParam String convName) {
        try {
            Long convId = conversationService.createGroupConversation(ownerId, convName);
            return ApiResponse.success("创建群聊会话成功", convId);
        } catch (Exception e) {
            return ApiResponse.serverError("创建群聊会话失败: " + e.getMessage());
        }
    }

    @PostMapping("/addMember")
    public ApiResponse<String> addMember(
            @RequestParam Long convId,
            @RequestParam Long userId,
            @RequestParam(required = false) String memberNickname) {
        try {
            int result = conversationService.addMemberToConversation(convId, userId, memberNickname);
            if (result > 0) {
                return ApiResponse.success("添加成员成功");
            } else {
                return ApiResponse.badRequest("添加成员失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("添加成员失败: " + e.getMessage());
        }
    }

    @GetMapping("/getMembers")
    public ApiResponse<List<ConversationMember>> getMembers(@RequestParam Long convId) {
        try {
            List<ConversationMember> members = conversationService.getMembersByConvId(convId);
            return ApiResponse.success("查询成功", members);
        } catch (Exception e) {
            return ApiResponse.serverError("查询成员失败: " + e.getMessage());
        }
    }

    @GetMapping("/getMemberCount")
    public ApiResponse<Integer> getMemberCount(@RequestParam Long convId) {
        try {
            Integer count = conversationService.getMemberCount(convId);
            return ApiResponse.success("查询成功", count);
        } catch (Exception e) {
            return ApiResponse.serverError("查询成员数量失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/removeMember")
    public ApiResponse<String> removeMember(
            @RequestParam Long convId,
            @RequestParam Long userId) {
        try {
            int result = conversationService.removeMember(convId, userId);
            if (result > 0) {
                return ApiResponse.success("移除成员成功");
            } else {
                return ApiResponse.badRequest("移除成员失败");
            }
        } catch (Exception e) {
            return ApiResponse.serverError("移除成员失败: " + e.getMessage());
        }
    }

    // 新增：更新群聊的已读回执设置
    @PostMapping("/updateReadReceiptSetting")
    public ApiResponse<String> updateReadReceiptSetting(
            @RequestParam Long convId,
            @RequestParam Boolean enableReadReceipt) {
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

    // 新增：查询群聊的已读回执设置
    @GetMapping("/getReadReceiptSetting")
    public ApiResponse<Boolean> getReadReceiptSetting(@RequestParam Long convId) {
        try {
            Boolean setting = conversationService.getReadReceiptSetting(convId);
            return ApiResponse.success("查询成功", setting);
        } catch (Exception e) {
            return ApiResponse.serverError("查询失败: " + e.getMessage());
        }
    }
}
package hrc.komuni.mapper;

import hrc.komuni.entity.Conversation;

public class ConversationMapperProvider {

    /**
     * 仅更新用户侧可修改的会话属性，不包含：
     * max_member_count, current_member_count, conv_status, create_time, update_time, conv_owner_id
     */
    public static String updateConversationAttriUserOrientedByConvId(Conversation conversation) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE conversation SET ");

        if (conversation.getConvType() != null) {
            sql.append("conv_type = #{convType}, ");
        }
        if (conversation.getConvName() != null) {
            sql.append("conv_name = #{convName}, ");
        }
        if (conversation.getConvAvatar() != null) {
            sql.append("conv_avatar = #{convAvatar}, ");
        }
        if (conversation.getConvDescription() != null) {
            sql.append("conv_description = #{convDescription}, ");
        }
        if (conversation.getEnableReadReceipt() != null) {
            sql.append("enable_read_receipt = #{enableReadReceipt}, ");
        }

        sql.append("update_time = NOW() WHERE conv_id = #{convId}");

        return sql.toString();
    }
}

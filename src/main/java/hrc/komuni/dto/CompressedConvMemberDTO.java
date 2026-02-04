package hrc.komuni.dto;

import lombok.Data;

@Data
public class CompressedConvMemberDTO {
    private Long userId;
    private String memberNickname; // 核心字段：若用户设置了群昵称则返回此值
    private String userNickname;   // 兜底字段：用户原始昵称
    private String userAvatar;
    private Integer role;          // 角色
}

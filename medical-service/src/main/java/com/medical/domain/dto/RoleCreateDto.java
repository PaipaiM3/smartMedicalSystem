package com.medical.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员新增角色
 */
@Data
public class RoleCreateDto {
    @NotBlank(message = "角色代码不能为空")
    @Size(min = 2, max = 50, message = "角色代码长度为 2～50 个字符")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "角色代码仅支持字母、数字与下划线")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最多 50 个字符")
    private String roleName;

    @Size(max = 200, message = "描述最多 200 个字符")
    private String description;

    /** 可选：1 启用 0 禁用，默认启用 */
    private Integer status;
}

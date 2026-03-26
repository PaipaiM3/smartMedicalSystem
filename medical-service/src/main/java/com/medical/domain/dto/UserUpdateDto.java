package com.medical.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息更新 DTO
 */
@Data
public class UserUpdateDto {
    private String name;
    private String email;
    private String mobilePhone;
    /** 传入则整体替换用户角色；不传表示不修改角色 */
    private List<Long> roleIds;
}

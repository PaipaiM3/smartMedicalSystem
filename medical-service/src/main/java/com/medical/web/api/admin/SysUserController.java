package com.medical.web.api.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.exception.BusinessWarningException;
import com.medical.common.exception.ServiceException;
import com.medical.common.pagination.PageResult;
import com.medical.common.response.ResultVo;
import com.medical.domain.dto.UserCreateDto;
import com.medical.domain.dto.UserUpdateDto;
import com.medical.domain.entity.SysRole;
import com.medical.domain.entity.SysUser;
import com.medical.domain.entity.SysUserRole;
import com.medical.domain.vo.UserListVo;
import com.medical.mapper.SysRoleMapper;
import com.medical.mapper.SysUserMapper;
import com.medical.mapper.SysUserRoleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    public ResultVo<PageResult<UserListVo>> page(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "roleCode", required = false) String roleCode) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getName, keyword)
                    .or().like(SysUser::getMobilePhone, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (StringUtils.hasText(roleCode)) {
            SysRole role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleCode, roleCode)
                            .eq(SysRole::getStatus, 1)
            );
            if (role == null) {
                PageResult<UserListVo> result = new PageResult<>();
                result.setCurrentPage(current);
                result.setPageSize(size);
                result.setTotal(0L);
                result.setList(Collections.emptyList());
                return ResultVo.ok(result);
            }
            List<Long> userIds = sysUserRoleMapper.selectList(
                            new LambdaQueryWrapper<SysUserRole>()
                                    .eq(SysUserRole::getRoleId, role.getRoleId())
                    )
                    .stream()
                    .map(SysUserRole::getUserId)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                PageResult<UserListVo> result = new PageResult<>();
                result.setCurrentPage(current);
                result.setPageSize(size);
                result.setTotal(0L);
                result.setList(Collections.emptyList());
                return ResultVo.ok(result);
            }
            wrapper.in(SysUser::getUserId, userIds);
        }
        wrapper.orderByDesc(SysUser::getCreatedTime);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(current, size), wrapper);
        List<UserListVo> voList = page.getRecords().stream().map(u -> {
            UserListVo vo = new UserListVo();
            vo.setUserId(u.getUserId());
            vo.setUsername(u.getUsername());
            vo.setName(u.getName());
            vo.setEmail(u.getEmail());
            vo.setMobilePhone(u.getMobilePhone());
            vo.setStatus(u.getStatus());
            vo.setCreatedTime(u.getCreatedTime());
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, u.getUserId()));
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            vo.setRoleIds(roleIds);
            List<String> roleNames = sysRoleMapper.selectRoleNamesByUserId(u.getUserId());
            vo.setRoleNames(roleNames != null ? roleNames : Collections.emptyList());
            return vo;
        }).collect(Collectors.toList());
        PageResult<UserListVo> result = new PageResult<>();
        result.setCurrentPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(voList);
        return ResultVo.ok(result);
    }

    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Void> create(@Valid @RequestBody UserCreateDto dto) {
        String username = dto.getUsername().trim();
        if (StringUtils.hasText(dto.getMobilePhone())
                && !dto.getMobilePhone().trim().matches("^1\\d{10}$")) {
            throw new BusinessWarningException("手机号格式不正确");
        }
        long exist = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (exist > 0) {
            throw new BusinessWarningException("用户名已被占用");
        }
        List<Long> roleIds = dto.getRoleIds().stream().distinct().collect(Collectors.toList());
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds)
        );
        if (roles.size() != roleIds.size()) {
            throw new BusinessWarningException("存在无效的角色编号");
        }
        for (SysRole r : roles) {
            if (r.getStatus() == null || r.getStatus() != 1) {
                throw new BusinessWarningException("不能分配已禁用的角色：" + r.getRoleName());
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!isSuperAdmin && roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode()))) {
            throw new BusinessWarningException("无权分配超级管理员角色");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(StringUtils.hasText(dto.getName()) ? dto.getName().trim() : username);
        user.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        user.setMobilePhone(StringUtils.hasText(dto.getMobilePhone()) ? dto.getMobilePhone().trim() : null);
        user.setStatus(1);
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        sysUserMapper.insert(user);

        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getUserId());
            ur.setRoleId(roleId);
            ur.setCreatedTime(now);
            sysUserRoleMapper.insert(ur);
        }
        return ResultVo.ok();
    }

    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Void> update(
            @PathVariable(value = "id") Long id,
            @RequestBody UserUpdateDto dto) {
        SysUser exist = sysUserMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("用户不存在");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, id);
        boolean hasUserField = false;
        if (dto.getName() != null) {
            wrapper.set(SysUser::getName, dto.getName());
            hasUserField = true;
        }
        if (dto.getEmail() != null) {
            wrapper.set(SysUser::getEmail, dto.getEmail());
            hasUserField = true;
        }
        if (dto.getMobilePhone() != null) {
            wrapper.set(SysUser::getMobilePhone, dto.getMobilePhone());
            hasUserField = true;
        }
        if (hasUserField) {
            sysUserMapper.update(null, wrapper);
        }

        if (dto.getRoleIds() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
            List<String> existingCodes = sysRoleMapper.selectRoleCodesByUserId(id);
            if (!isSuperAdmin && existingCodes != null && existingCodes.contains("SUPER_ADMIN")) {
                throw new BusinessWarningException("无权修改超级管理员的角色");
            }
            List<Long> roleIds = dto.getRoleIds().stream().distinct().collect(Collectors.toList());
            if (roleIds.isEmpty()) {
                throw new BusinessWarningException("请至少选择一个角色");
            }
            List<SysRole> roles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds));
            if (roles.size() != roleIds.size()) {
                throw new BusinessWarningException("存在无效的角色编号");
            }
            for (SysRole r : roles) {
                if (r.getStatus() == null || r.getStatus() != 1) {
                    throw new BusinessWarningException("不能分配已禁用的角色：" + r.getRoleName());
                }
            }
            if (!isSuperAdmin && roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode()))) {
                throw new BusinessWarningException("无权分配超级管理员角色");
            }
            sysUserRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
            LocalDateTime now = LocalDateTime.now();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                ur.setCreatedTime(now);
                sysUserRoleMapper.insert(ur);
            }
        }
        return ResultVo.ok();
    }

    @PutMapping("/{id}/status")
    public ResultVo<Void> updateStatus(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "status") Integer status) {
        SysUser exist = sysUserMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("用户不存在");
        }
        if (status != 0 && status != 1) {
            throw new ServiceException("状态值无效");
        }
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getUserId, id)
                .set(SysUser::getStatus, status));
        return ResultVo.ok();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Void> delete(@PathVariable(value = "id") Long id) {
        SysUser exist = sysUserMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("用户不存在");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && exist.getUsername().equals(auth.getName())) {
            throw new BusinessWarningException("不能删除当前登录账号");
        }
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(id);
        if (!isSuperAdmin && roleCodes != null && roleCodes.contains("SUPER_ADMIN")) {
            throw new BusinessWarningException("无权删除超级管理员账号");
        }
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        sysUserMapper.deleteById(id);
        return ResultVo.ok();
    }
}

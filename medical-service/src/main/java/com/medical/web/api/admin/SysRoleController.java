package com.medical.web.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.exception.BusinessWarningException;
import com.medical.common.exception.ServiceException;
import com.medical.common.pagination.PageResult;
import com.medical.common.response.ResultVo;
import com.medical.domain.dto.RoleCreateDto;
import com.medical.domain.dto.RoleUpdateDto;
import com.medical.domain.entity.SysRole;
import com.medical.domain.entity.SysUserRole;
import com.medical.domain.vo.RoleListVo;
import com.medical.mapper.SysRoleMapper;
import com.medical.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理 API
 */
@RestController
@RequestMapping("/api/admin/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @PostMapping
    public ResultVo<Void> create(@Valid @RequestBody RoleCreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!isSuperAdmin) {
            throw new BusinessWarningException("仅超级管理员可新增角色");
        }

        String code = dto.getRoleCode().trim().toUpperCase();
        long exist = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
        if (exist > 0) {
            throw new BusinessWarningException("角色代码已存在");
        }
        int status = 1;
        if (dto.getStatus() != null) {
            if (dto.getStatus() != 0 && dto.getStatus() != 1) {
                throw new ServiceException("状态值无效");
            }
            status = dto.getStatus();
        }
        LocalDateTime now = LocalDateTime.now();
        SysRole role = new SysRole();
        role.setRoleCode(code);
        role.setRoleName(dto.getRoleName().trim());
        role.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        role.setStatus(status);
        role.setCreatedTime(now);
        role.setUpdatedTime(now);
        sysRoleMapper.insert(role);
        return ResultVo.ok();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Void> delete(@PathVariable(value = "id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!isSuperAdmin) {
            throw new BusinessWarningException("仅超级管理员可删除角色");
        }
        SysRole exist = sysRoleMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("角色不存在");
        }
        if ("SUPER_ADMIN".equals(exist.getRoleCode())) {
            throw new BusinessWarningException("系统保留角色不可删除");
        }
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        sysRoleMapper.deleteById(id);
        return ResultVo.ok();
    }

    @GetMapping("/page")
    public ResultVo<PageResult<RoleListVo>> page(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getRoleId);
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(current, size), wrapper);
        List<RoleListVo> voList = page.getRecords().stream().map(r -> {
            RoleListVo vo = new RoleListVo();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        PageResult<RoleListVo> result = new PageResult<>();
        result.setCurrentPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(voList);
        return ResultVo.ok(result);
    }

    @GetMapping("/list")
    public ResultVo<List<RoleListVo>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        wrapper.orderByAsc(SysRole::getRoleId);
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        List<RoleListVo> voList = roles.stream().map(r -> {
            RoleListVo vo = new RoleListVo();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
        return ResultVo.ok(voList);
    }

    @PutMapping("/{id}")
    public ResultVo<Void> update(
            @PathVariable(value = "id") Long id,
            @RequestBody RoleUpdateDto dto) {
        SysRole exist = sysRoleMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("角色不存在");
        }
        LambdaUpdateWrapper<SysRole> wrapper = new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getRoleId, id);
        if (dto.getRoleName() != null) wrapper.set(SysRole::getRoleName, dto.getRoleName());
        if (dto.getDescription() != null) wrapper.set(SysRole::getDescription, dto.getDescription());
        sysRoleMapper.update(null, wrapper);
        return ResultVo.ok();
    }

    @PutMapping("/{id}/status")
    public ResultVo<Void> updateStatus(
            @PathVariable(value = "id") Long id,
            @RequestParam(value = "status") Integer status) {
        SysRole exist = sysRoleMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("角色不存在");
        }
        if (status != 0 && status != 1) {
            throw new ServiceException("状态值无效");
        }
        sysRoleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getRoleId, id)
                .set(SysRole::getStatus, status));
        return ResultVo.ok();
    }

}

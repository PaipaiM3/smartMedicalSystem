package com.medical.web.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.exception.BusinessWarningException;
import com.medical.common.exception.ServiceException;
import com.medical.common.pagination.PageResult;
import com.medical.common.response.ResultVo;
import com.medical.domain.dto.MedicineCreateDto;
import com.medical.domain.dto.MedicineUpdateDto;
import com.medical.domain.entity.Medicine;
import com.medical.domain.entity.MedicineCategory;
import com.medical.domain.vo.MedicineCategoryVo;
import com.medical.domain.vo.MedicineDetailVo;
import com.medical.domain.vo.MedicineListVo;
import com.medical.mapper.MedicineCategoryMapper;
import com.medical.mapper.MedicineMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 药品管理 API（列表 / 分类）
 */
@RestController
@RequestMapping("/api/admin/medicine")
@RequiredArgsConstructor
public class SysMedicineController {

    private final MedicineMapper medicineMapper;
    private final MedicineCategoryMapper medicineCategoryMapper;

    @PostMapping
    public ResultVo<Void> create(@Valid @RequestBody MedicineCreateDto dto) {
        String code = dto.getMedicineCode().trim();
        long dup = medicineMapper.selectCount(
                new LambdaQueryWrapper<Medicine>().eq(Medicine::getMedicineCode, code));
        if (dup > 0) {
            throw new BusinessWarningException("药品编码已存在");
        }
        MedicineCategory cat = medicineCategoryMapper.selectById(dto.getCategoryId());
        if (cat == null || cat.getStatus() == null || cat.getStatus() != 1) {
            throw new BusinessWarningException("请选择有效的药品分类");
        }
        int status = dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1;
        if (dto.getStockQuantity() < 0 || dto.getMinStock() < 0) {
            throw new BusinessWarningException("库存数量不能为负数");
        }
        LocalDateTime now = LocalDateTime.now();
        Medicine m = new Medicine();
        m.setMedicineCode(code);
        m.setName(dto.getName().trim());
        m.setCommonName(StringUtils.hasText(dto.getCommonName()) ? dto.getCommonName().trim() : null);
        m.setCategoryId(dto.getCategoryId());
        m.setSpec(dto.getSpec().trim());
        m.setUnit(dto.getUnit().trim());
        m.setManufacturer(dto.getManufacturer().trim());
        m.setApprovalNo(StringUtils.hasText(dto.getApprovalNo()) ? dto.getApprovalNo().trim() : null);
        m.setUnitPrice(dto.getUnitPrice());
        m.setCostPrice(dto.getCostPrice());
        m.setStockQuantity(dto.getStockQuantity());
        m.setMinStock(dto.getMinStock());
        m.setStatus(status);
        m.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        m.setCreatedTime(now);
        m.setUpdatedTime(now);
        medicineMapper.insert(m);
        return ResultVo.ok();
    }

    @GetMapping("/{id}")
    public ResultVo<MedicineDetailVo> detail(@PathVariable("id") Long id) {
        Medicine m = medicineMapper.selectById(id);
        if (m == null) {
            throw new ServiceException("药品不存在");
        }
        MedicineDetailVo vo = new MedicineDetailVo();
        BeanUtils.copyProperties(m, vo);
        if (m.getCategoryId() != null) {
            MedicineCategory c = medicineCategoryMapper.selectById(m.getCategoryId());
            if (c != null) {
                vo.setCategoryName(c.getName());
            }
        }
        return ResultVo.ok(vo);
    }

    @PutMapping("/{id}")
    public ResultVo<Void> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicineUpdateDto dto) {
        Medicine exist = medicineMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("药品不存在");
        }
        MedicineCategory cat = medicineCategoryMapper.selectById(dto.getCategoryId());
        if (cat == null || cat.getStatus() == null || cat.getStatus() != 1) {
            throw new BusinessWarningException("请选择有效的药品分类");
        }
        if (dto.getStockQuantity() < 0 || dto.getMinStock() < 0) {
            throw new BusinessWarningException("库存数量不能为负数");
        }
        int status = dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1;
        exist.setName(dto.getName().trim());
        exist.setCommonName(StringUtils.hasText(dto.getCommonName()) ? dto.getCommonName().trim() : null);
        exist.setCategoryId(dto.getCategoryId());
        exist.setSpec(dto.getSpec().trim());
        exist.setUnit(dto.getUnit().trim());
        exist.setManufacturer(dto.getManufacturer().trim());
        exist.setApprovalNo(StringUtils.hasText(dto.getApprovalNo()) ? dto.getApprovalNo().trim() : null);
        exist.setUnitPrice(dto.getUnitPrice());
        exist.setCostPrice(dto.getCostPrice());
        exist.setStockQuantity(dto.getStockQuantity());
        exist.setMinStock(dto.getMinStock());
        exist.setStatus(status);
        exist.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        exist.setUpdatedTime(LocalDateTime.now());
        medicineMapper.updateById(exist);
        return ResultVo.ok();
    }

    @GetMapping("/page")
    public ResultVo<PageResult<MedicineListVo>> page(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Medicine::getName, kw)
                    .or().like(Medicine::getCommonName, kw)
                    .or().like(Medicine::getMedicineCode, kw));
        }
        if (categoryId != null) {
            wrapper.eq(Medicine::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Medicine::getStatus, status);
        }
        wrapper.orderByAsc(Medicine::getMedicineId);
        Page<Medicine> page = medicineMapper.selectPage(new Page<>(current, size), wrapper);

        Set<Long> catIds = page.getRecords().stream()
                .map(Medicine::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            List<MedicineCategory> cats = medicineCategoryMapper.selectBatchIds(catIds);
            for (MedicineCategory c : cats) {
                categoryNameMap.put(c.getCategoryId(), c.getName());
            }
        }

        List<MedicineListVo> voList = page.getRecords().stream().map(m -> {
            MedicineListVo vo = new MedicineListVo();
            BeanUtils.copyProperties(m, vo);
            if (m.getCategoryId() != null) {
                vo.setCategoryName(categoryNameMap.get(m.getCategoryId()));
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult<MedicineListVo> result = new PageResult<>();
        result.setCurrentPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(voList);
        return ResultVo.ok(result);
    }

    /**
     * 药品分类列表（启用），用于筛选下拉
     */
    @GetMapping("/categories")
    public ResultVo<List<MedicineCategoryVo>> categories() {
        List<MedicineCategory> list = medicineCategoryMapper.selectList(
                new LambdaQueryWrapper<MedicineCategory>()
                        .eq(MedicineCategory::getStatus, 1)
                        .orderByAsc(MedicineCategory::getSortOrder)
                        .orderByAsc(MedicineCategory::getCategoryId));
        List<MedicineCategoryVo> voList = list.stream().map(c -> {
            MedicineCategoryVo vo = new MedicineCategoryVo();
            vo.setCategoryId(c.getCategoryId());
            vo.setName(c.getName());
            return vo;
        }).collect(Collectors.toList());
        return ResultVo.ok(voList);
    }
}

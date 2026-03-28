package com.medical.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增药品
 */
@Data
public class MedicineCreateDto {
    @NotBlank(message = "药品编码不能为空")
    @Size(max = 50, message = "药品编码最多 50 个字符")
    private String medicineCode;

    @NotBlank(message = "药品名称不能为空")
    @Size(max = 100, message = "药品名称最多 100 个字符")
    private String name;

    @Size(max = 100, message = "通用名最多 100 个字符")
    private String commonName;

    @NotNull(message = "请选择药品分类")
    private Long categoryId;

    @NotBlank(message = "规格不能为空")
    @Size(max = 100, message = "规格最多 100 个字符")
    private String spec;

    @NotBlank(message = "单位不能为空")
    @Size(max = 20, message = "单位最多 20 个字符")
    private String unit;

    @NotBlank(message = "生产厂家不能为空")
    @Size(max = 200, message = "生产厂家最多 200 个字符")
    private String manufacturer;

    @Size(max = 100, message = "批准文号最多 100 个字符")
    private String approvalNo;

    @NotNull(message = "请填写零售价")
    @DecimalMin(value = "0.0", inclusive = true, message = "零售价不能为负数")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "成本价不能为负数")
    private BigDecimal costPrice;

    @NotNull(message = "请填写库存数量")
    private Integer stockQuantity;

    @NotNull(message = "请填写最低库存")
    private Integer minStock;

    /** 1 在用 0 停用，默认 1 */
    private Integer status;

    @Size(max = 500, message = "备注最多 500 个字符")
    private String remark;
}

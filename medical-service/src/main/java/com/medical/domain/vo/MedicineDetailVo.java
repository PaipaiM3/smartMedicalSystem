package com.medical.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品详情 VO
 */
@Data
public class MedicineDetailVo {
    private Long medicineId;
    private String medicineCode;
    private String name;
    private String commonName;
    private Long categoryId;
    private String categoryName;
    private String spec;
    private String unit;
    private String manufacturer;
    private String approvalNo;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private Integer minStock;
    private Integer status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}

package com.medical.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleSaveDto {
    @NotNull(message = "医生不能为空")
    private Long doctorId;

    @NotNull(message = "排班日期不能为空")
    private LocalDate scheduleDate;

    @NotBlank(message = "时段不能为空")
    @Size(max = 20, message = "时段长度过长")
    private String timeSlot;

    @NotNull(message = "总号源不能为空")
    @Min(value = 1, message = "总号源至少为1")
    @Max(value = 999, message = "总号源不能超过999")
    private Integer totalSlots;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    @Size(max = 500, message = "备注过长")
    private String remark;
}

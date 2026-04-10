package com.medical.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleListVo {
    private Long scheduleId;
    private Long doctorId;
    private Long deptId;
    private String deptName;
    private String doctorName;
    private String doctorTitle;
    private String scheduleTimeSlot;
    private Integer totalSlots;
    private Integer bookedSlots;
    private Integer remainingSlots;
    private Integer status;
    private String statusText;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}

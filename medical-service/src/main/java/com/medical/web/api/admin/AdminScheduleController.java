package com.medical.web.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.common.exception.BusinessWarningException;
import com.medical.common.exception.ServiceException;
import com.medical.common.pagination.PageResult;
import com.medical.common.response.ResultVo;
import com.medical.domain.dto.ScheduleSaveDto;
import com.medical.domain.entity.Appointment;
import com.medical.domain.entity.Doctor;
import com.medical.domain.entity.Schedule;
import com.medical.domain.entity.SysDept;
import com.medical.domain.vo.ScheduleListVo;
import com.medical.mapper.AppointmentMapper;
import com.medical.mapper.DoctorMapper;
import com.medical.mapper.ScheduleMapper;
import com.medical.mapper.SysDeptMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final SysDeptMapper sysDeptMapper;
    private final AppointmentMapper appointmentMapper;

    @GetMapping("/list")
    public ResultVo<PageResult<ScheduleListVo>> list(
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            wrapper.eq(Schedule::getDeptId, deptId);
        }
        if (doctorId != null) {
            wrapper.eq(Schedule::getDoctorId, doctorId);
        }
        if (date != null) {
            wrapper.eq(Schedule::getScheduleDate, date);
        }
        if (status != null) {
            wrapper.eq(Schedule::getStatus, status);
        }
        wrapper.orderByDesc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getTimeSlot)
                .orderByAsc(Schedule::getScheduleId);
        Page<Schedule> page = scheduleMapper.selectPage(new Page<>(current, size), wrapper);

        Set<Long> doctorIds = page.getRecords().stream()
                .map(Schedule::getDoctorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Doctor> doctorMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            for (Doctor d : doctorMapper.selectBatchIds(doctorIds)) {
                if (d != null) {
                    doctorMap.put(d.getDoctorId(), d);
                }
            }
        }
        Set<Long> deptIds = page.getRecords().stream()
                .map(Schedule::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            for (SysDept d : sysDeptMapper.selectBatchIds(deptIds)) {
                if (d != null) {
                    deptNameMap.put(d.getDeptId(), d.getName());
                }
            }
        }

        List<ScheduleListVo> list = page.getRecords().stream().map(s -> {
            ScheduleListVo vo = new ScheduleListVo();
            vo.setScheduleId(s.getScheduleId());
            vo.setDoctorId(s.getDoctorId());
            vo.setDeptId(s.getDeptId());
            vo.setDeptName(s.getDeptId() != null ? deptNameMap.get(s.getDeptId()) : null);
            Doctor doctor = doctorMap.get(s.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                vo.setDoctorTitle(doctor.getTitle());
            }
            vo.setScheduleDate(s.getScheduleDate());
            vo.setScheduleTimeSlot(s.getTimeSlot());
            vo.setTotalSlots(s.getTotalSlots());
            vo.setBookedSlots(s.getBookedSlots());
            int totalSlots = s.getTotalSlots() != null ? s.getTotalSlots() : 0;
            int bookedSlots = s.getBookedSlots() != null ? s.getBookedSlots() : 0;
            vo.setRemainingSlots(Math.max(0, totalSlots - bookedSlots));
            vo.setStatus(s.getStatus());
            vo.setStatusText(s.getStatus() != null && s.getStatus() == 1 ? "可预约" : "停诊");
            vo.setRemark(s.getRemark());
            vo.setCreatedTime(s.getCreatedTime());
            return vo;
        }).collect(Collectors.toList());

        PageResult<ScheduleListVo> result = new PageResult<>();
        result.setCurrentPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setList(list);
        return ResultVo.ok(result);
    }

    @PostMapping
    public ResultVo<Void> create(@Valid @RequestBody ScheduleSaveDto dto) {
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null || doctor.getStatus() == null || doctor.getStatus() != 1) {
            throw new BusinessWarningException("医生不存在或已停用");
        }
        if (doctor.getDeptId() == null) {
            throw new BusinessWarningException("该医生未绑定科室，无法排班");
        }
        validateUnique(dto.getDoctorId(), dto.getScheduleDate(), dto.getTimeSlot(), null);

        LocalDateTime now = LocalDateTime.now();
        Schedule s = new Schedule();
        s.setDoctorId(dto.getDoctorId());
        s.setDeptId(doctor.getDeptId());
        s.setScheduleDate(dto.getScheduleDate());
        s.setTimeSlot(dto.getTimeSlot().trim());
        s.setTotalSlots(dto.getTotalSlots());
        s.setBookedSlots(0);
        s.setStatus(dto.getStatus());
        s.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        s.setCreatedTime(now);
        s.setUpdatedTime(now);
        scheduleMapper.insert(s);
        return ResultVo.ok();
    }

    @PutMapping("/{id}")
    public ResultVo<Void> update(@PathVariable("id") Long id, @Valid @RequestBody ScheduleSaveDto dto) {
        Schedule exist = scheduleMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("排班不存在");
        }
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null || doctor.getStatus() == null || doctor.getStatus() != 1) {
            throw new BusinessWarningException("医生不存在或已停用");
        }
        if (doctor.getDeptId() == null) {
            throw new BusinessWarningException("该医生未绑定科室，无法排班");
        }
        validateUnique(dto.getDoctorId(), dto.getScheduleDate(), dto.getTimeSlot(), id);

        int newTotal = dto.getTotalSlots();
        int booked = exist.getBookedSlots() != null ? exist.getBookedSlots() : 0;
        if (newTotal < booked) {
            throw new BusinessWarningException("总号源不能小于已预约数（" + booked + "）");
        }

        exist.setDoctorId(dto.getDoctorId());
        exist.setDeptId(doctor.getDeptId());
        exist.setScheduleDate(dto.getScheduleDate());
        exist.setTimeSlot(dto.getTimeSlot().trim());
        exist.setTotalSlots(newTotal);
        exist.setStatus(dto.getStatus());
        exist.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        exist.setUpdatedTime(LocalDateTime.now());
        scheduleMapper.updateById(exist);
        return ResultVo.ok();
    }

    @DeleteMapping("/{id}")
    public ResultVo<Void> delete(@PathVariable("id") Long id) {
        Schedule exist = scheduleMapper.selectById(id);
        if (exist == null) {
            throw new ServiceException("排班不存在");
        }
        long activeAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getScheduleId, id)
                        .in(Appointment::getStatus, List.of(1, 2))
        );
        if (activeAppointments > 0) {
            throw new BusinessWarningException("该排班下存在待就诊/已就诊预约，无法删除");
        }
        scheduleMapper.deleteById(id);
        return ResultVo.ok();
    }

    private void validateUnique(Long doctorId, LocalDate date, String timeSlot, Long ignoreId) {
        String normalizedSlot = timeSlot == null ? null : timeSlot.trim();
        LambdaQueryWrapper<Schedule> w = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getScheduleDate, date)
                .eq(Schedule::getTimeSlot, normalizedSlot);
        if (ignoreId != null) {
            w.ne(Schedule::getScheduleId, ignoreId);
        }
        long dup = scheduleMapper.selectCount(w);
        if (dup > 0) {
            throw new BusinessWarningException("该医生在该日期该时段已存在排班");
        }
    }
}

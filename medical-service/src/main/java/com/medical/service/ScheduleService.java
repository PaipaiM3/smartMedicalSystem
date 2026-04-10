package com.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.medical.domain.entity.Doctor;
import com.medical.domain.entity.Schedule;
import com.medical.domain.vo.ScheduleSlotVo;
import com.medical.mapper.DoctorMapper;
import com.medical.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;

    /**
     * 获取医生的可预约日期（未来7天内有排班的日期）
     */
    public List<LocalDate> getAvailableDates(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return List.of();
        }
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getStatus, 1)
                .ge(Schedule::getScheduleDate, startDate)
                .le(Schedule::getScheduleDate, endDate)
                .orderByAsc(Schedule::getScheduleDate);
        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(Schedule::getScheduleDate)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取医生某天的排班时段
     */
    public List<ScheduleSlotVo> getScheduleSlots(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getScheduleDate, date)
                .eq(Schedule::getStatus, 1)
                .orderByAsc(Schedule::getTimeSlot);
        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        return schedules.stream().map(s -> {
            ScheduleSlotVo vo = new ScheduleSlotVo();
            vo.setScheduleId(s.getScheduleId());
            vo.setTimeSlot(s.getTimeSlot());
            vo.setTotal(s.getTotalSlots());
            vo.setBooked(s.getBookedSlots());
            vo.setRemaining(s.getTotalSlots() - s.getBookedSlots());
            vo.setAvailable(s.getBookedSlots() < s.getTotalSlots());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取排班信息
     */
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleMapper.selectById(scheduleId);
    }

    /**
     * 锁定号源（增加已预约数）
     */
    public boolean lockSlot(Long scheduleId) {
        // 原子条件更新：仅当 status=1 且 booked_slots < total_slots 时 +1，避免并发超卖
        int updated = scheduleMapper.update(
                null,
                new LambdaUpdateWrapper<Schedule>()
                        .eq(Schedule::getScheduleId, scheduleId)
                        .eq(Schedule::getStatus, 1)
                        .apply("booked_slots < total_slots")
                        .setSql("booked_slots = booked_slots + 1")
                        .set(Schedule::getUpdatedTime, LocalDateTime.now())
        );
        return updated > 0;
    }

    /**
     * 释放号源（取消预约时使用）
     */
    public boolean releaseSlot(Long scheduleId) {
        int updated = scheduleMapper.update(
                null,
                new LambdaUpdateWrapper<Schedule>()
                        .eq(Schedule::getScheduleId, scheduleId)
                        .apply("booked_slots > 0")
                        .setSql("booked_slots = booked_slots - 1")
                        .set(Schedule::getUpdatedTime, LocalDateTime.now())
        );
        return updated > 0;
    }
}
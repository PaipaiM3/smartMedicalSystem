package com.medical.web.api.reception;

import com.medical.common.response.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 挂号收费端占位接口，后续替换为真实业务。
 */
@RestController
@RequestMapping("/api/reception")
public class ReceptionStubController {

    @GetMapping("/ping")
    public ResultVo<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", "reception");
        data.put("status", "ok");
        return ResultVo.ok(data);
    }

    @GetMapping("/appointment/summary")
    public ResultVo<Map<String, Object>> appointmentSummary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", Collections.emptyList());
        data.put("total", 0);
        data.put("message", "占位：预约列表接口待实现");
        return ResultVo.ok(data);
    }
}

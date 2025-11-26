package com.example.backend.controller;

import com.example.backend.service.PythonDataService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/python-data")
public class PythonDataController {

    private final PythonDataService pythonDataService;

    public PythonDataController(PythonDataService pythonDataService) {
        this.pythonDataService = pythonDataService;
    }

    @PostMapping("/fund-data")
    public ResponseEntity<?> getFundData(HttpSession session) {
        // 检查会话（如果需要认证）
        System.out.println(1);
        if (session == null || session.getAttribute("userId") == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "未登录或会话已过期");
            return ResponseEntity.status(401).body(resp);
        }

        // 调用Python服务获取数据
        Map<String, Object> result = pythonDataService.getFundData();
        
        // 数据已经在Service中打印到日志了
        // 这里返回给前端
        return ResponseEntity.ok(result);
    }
}


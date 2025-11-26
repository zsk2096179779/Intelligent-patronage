package com.example.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class PythonDataService {

    private static final Logger logger = LoggerFactory.getLogger(PythonDataService.class);
    private final ObjectMapper objectMapper;

    public PythonDataService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 执行Python脚本获取基金数据
     * @return 包含基金数据的Map
     */
    public Map<String, Object> getFundData() {
        try {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            Path pythonScriptPath = Paths.get(projectRoot, "python", "fund_data_service.py");
            
            logger.info("执行Python脚本: {}", pythonScriptPath);
            
            // 构建Python命令
            // 假设Python在系统PATH中，如果需要指定Python路径，可以修改这里
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",  // 或者 "python3" 根据系统配置
                pythonScriptPath.toString()
            );
            
            // 设置工作目录
            processBuilder.directory(Paths.get(projectRoot).toFile());
            
            // 启动进程
            Process process = processBuilder.start();
            
            // 读取标准输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 读取错误输出
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            String outputStr = output.toString().trim();
            String errorStr = errorOutput.toString();
            
            // 打印输出（用于调试）
            logger.info("Python脚本执行完成，退出码: {}", exitCode);
            logger.info("Python输出: {}", outputStr);
            if (!errorStr.isEmpty()) {
                logger.warn("Python错误输出: {}", errorStr);
            }
            
            if (exitCode != 0) {
                logger.error("Python脚本执行失败，退出码: {}", exitCode);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Python脚本执行失败: " + errorStr);
                return errorResult;
            }
            
            // 解析JSON输出
            if (outputStr.isEmpty()) {
                logger.error("Python脚本没有输出数据");
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Python脚本没有返回数据");
                return errorResult;
            }
            
            // 解析JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(outputStr, Map.class);
            
            // 打印解析后的数据
            logger.info("成功获取基金数据:");
            logger.info("总记录数: {}", result.get("total_count"));
            logger.info("返回记录数: {}", result.get("returned_count"));
            
            if (result.containsKey("data")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> dataList = 
                    (java.util.List<Map<String, Object>>) result.get("data");
                logger.info("数据示例（前3条）:");
                int count = Math.min(3, dataList.size());
                for (int i = 0; i < count; i++) {
                    logger.info("  记录 {}: {}", i + 1, dataList.get(i));
                }
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("执行Python脚本时发生错误", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "执行Python脚本失败: " + e.getMessage());
            return errorResult;
        }
    }
}


package com.example.backend.service;

import com.example.backend.entity.StrategyWarning;
import com.example.backend.mapper.StrategyWarningMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StrategyWarningService {

    private static final Logger logger = LoggerFactory.getLogger(StrategyWarningService.class);
    private final StrategyWarningMapper warningMapper;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Value("${python.executable:}")
    private String configuredPythonPath;

    public StrategyWarningService(StrategyWarningMapper warningMapper) {
        this.warningMapper = warningMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从 Python 脚本获取预警数据并保存到数据库
     * @param strategyId 策略ID，如果为null则保存为通用预警（strategy_id=0）
     * @return 保存的预警数量
     */
    @Transactional
    public int fetchAndSaveWarnings(Long strategyId) {
        // 先删除该策略的旧预警数据，避免重复
        Long targetStrategyId = strategyId != null ? strategyId : 0L;
        warningMapper.deleteByStrategyId(targetStrategyId);
        logger.info("已删除策略 {} 的旧预警数据", targetStrategyId);
        try {
            // 获取项目根目录（user.dir 可能是 backend 目录，需要向上查找）
            String currentDir = System.getProperty("user.dir");
            Path projectRoot = Paths.get(currentDir);
            
            // 如果当前在 backend 目录，向上找到项目根目录
            if (projectRoot.getFileName().toString().equals("backend")) {
                projectRoot = projectRoot.getParent();
                logger.info("检测到在backend目录，向上查找项目根目录: {}", projectRoot);
            }
            
            Path pythonScriptPath = projectRoot.resolve("python").resolve("test.py");
            
            // 检查文件是否存在
            if (!pythonScriptPath.toFile().exists()) {
                logger.error("Python脚本不存在: {}", pythonScriptPath);
                logger.error("请确保脚本路径正确，当前项目根目录: {}", projectRoot);
                return 0;
            }
            
            logger.info("执行Python脚本获取预警数据: {}", pythonScriptPath);
            
            // 检测可用的Python命令
            String pythonCommand = detectPythonCommand();
            if (pythonCommand == null) {
                logger.error("未找到Python命令，请确保:");
                logger.error("1. Python已安装");
                logger.error("2. Python已添加到系统PATH，或");
                logger.error("3. 在 application.properties 中配置 python.executable=完整路径");
                return 0;
            }
            
            logger.info("使用Python命令: {}", pythonCommand);
            
            // 构建Python命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonCommand,
                pythonScriptPath.toString()
            );
            
            // 设置工作目录为项目根目录
            processBuilder.directory(projectRoot.toFile());
            
            // 设置环境变量
            processBuilder.environment().put("PYTHONUNBUFFERED", "1");
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");  // 设置Python输出编码为UTF-8
            
            // 启动进程
            Process process = processBuilder.start();
            
            // 读取标准输出（确保使用UTF-8编码）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 读取错误输出（确保使用UTF-8编码）
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            String outputStr = output.toString().trim();
            String errorStr = errorOutput.toString();
            
            logger.info("Python脚本执行完成，退出码: {}", exitCode);
            if (!errorStr.isEmpty()) {
                logger.warn("Python错误输出: {}", errorStr);
            }
            
            if (exitCode != 0) {
                logger.error("Python脚本执行失败，退出码: {}", exitCode);
                if (exitCode == 9009) {
                    logger.error("错误: 系统找不到Python命令。请确保:");
                    logger.error("1. Python已安装");
                    logger.error("2. Python已添加到系统PATH环境变量");
                    logger.error("3. 或者使用 'python3' 或 'py' 命令");
                }
                if (!errorStr.isEmpty()) {
                    logger.error("Python错误输出: {}", errorStr);
                }
                return 0;
            }
            
            // 解析JSON输出
            if (outputStr.isEmpty()) {
                logger.warn("Python脚本没有输出数据");
                return 0;
            }
            
            // 从输出中提取JSON部分（Python脚本可能输出其他信息）
            String jsonStr = extractJsonFromOutput(outputStr);
            if (jsonStr == null || jsonStr.isEmpty()) {
                logger.warn("未能从Python输出中提取JSON数据");
                return 0;
            }
            
            // 解析JSON
            List<Map<String, Object>> warningsData = parseWarningsJson(jsonStr);
            if (warningsData == null || warningsData.isEmpty()) {
                logger.info("Python脚本未发现预警信息");
                return 0;
            }
            
            // 转换为实体并保存
            List<StrategyWarning> warnings = convertToWarnings(warningsData, strategyId);
            if (!warnings.isEmpty()) {
                warningMapper.insertBatch(warnings);
                logger.info("成功保存 {} 条预警信息", warnings.size());
            }
            
            return warnings.size();
            
        } catch (Exception e) {
            logger.error("获取并保存预警数据时发生错误", e);
            return 0;
        }
    }

    /**
     * 从Python输出中提取JSON部分
     * 处理可能包含调试信息的情况
     */
    private String extractJsonFromOutput(String output) {
        if (output == null || output.trim().isEmpty()) {
            return null;
        }
        
        // 移除首尾空白
        String trimmed = output.trim();
        
        // 尝试查找JSON数组的开始和结束（优先）
        int jsonStart = trimmed.indexOf('[');
        if (jsonStart >= 0) {
            // 从 [ 开始，找到匹配的 ]
            int bracketCount = 0;
            int jsonEnd = -1;
            for (int i = jsonStart; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
            if (jsonEnd > jsonStart) {
                String jsonStr = trimmed.substring(jsonStart, jsonEnd + 1);
                logger.debug("从输出中提取到JSON数组: {}", jsonStr.substring(0, Math.min(100, jsonStr.length())));
                return jsonStr;
            }
        }
        
        // 如果没有找到数组，尝试查找对象
        jsonStart = trimmed.indexOf('{');
        if (jsonStart >= 0) {
            // 从 { 开始，找到匹配的 }
            int braceCount = 0;
            int jsonEnd = -1;
            for (int i = jsonStart; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
            if (jsonEnd > jsonStart) {
                String jsonStr = trimmed.substring(jsonStart, jsonEnd + 1);
                logger.debug("从输出中提取到JSON对象: {}", jsonStr.substring(0, Math.min(100, jsonStr.length())));
                return jsonStr;
            }
        }
        
        // 如果都找不到，尝试直接解析整个输出（可能是纯JSON）
        logger.warn("未能从输出中提取JSON，尝试直接解析整个输出");
        logger.debug("原始输出: {}", output.substring(0, Math.min(200, output.length())));
        return trimmed;
    }

    /**
     * 解析预警JSON数据
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseWarningsJson(String jsonStr) {
        try {
            Object parsed = objectMapper.readValue(jsonStr, Object.class);
            
            if (parsed instanceof List) {
                return (List<Map<String, Object>>) parsed;
            } else if (parsed instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) parsed;
                // 查找包含列表的键
                for (Object value : map.values()) {
                    if (value instanceof List) {
                        return (List<Map<String, Object>>) value;
                    }
                }
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("解析预警JSON失败: {}", jsonStr, e);
            return new ArrayList<>();
        }
    }

    /**
     * 将Python返回的数据转换为StrategyWarning实体
     */
    private List<StrategyWarning> convertToWarnings(List<Map<String, Object>> warningsData, Long strategyId) {
        List<StrategyWarning> warnings = new ArrayList<>();
        
        for (Map<String, Object> data : warningsData) {
            try {
                StrategyWarning warning = new StrategyWarning();
                
                // 设置策略ID，如果为null则设为0（通用预警）
                warning.setStrategyId(strategyId != null ? strategyId : 0L);
                
                // 标题
                warning.setTitle(getStringValue(data, "title", "预警信息"));
                
                // 描述
                warning.setDescription(getStringValue(data, "description", ""));
                
                // 风险等级
                String riskLevel = getStringValue(data, "risk_level", "low").toLowerCase();
                if (!riskLevel.equals("high") && !riskLevel.equals("medium") && !riskLevel.equals("low")) {
                    riskLevel = "low";
                }
                warning.setRiskLevel(riskLevel);
                
                // 事件时间
                String eventTimeStr = getStringValue(data, "event_time", null);
                if (eventTimeStr != null && !eventTimeStr.isEmpty()) {
                    try {
                        // 尝试解析时间字符串
                        LocalDateTime eventTime = parseDateTime(eventTimeStr);
                        warning.setEventTime(eventTime);
                    } catch (Exception e) {
                        logger.warn("解析事件时间失败: {}, 使用当前时间", eventTimeStr);
                        warning.setEventTime(LocalDateTime.now());
                    }
                } else {
                    warning.setEventTime(LocalDateTime.now());
                }
                
                // 默认未解决
                warning.setResolved(false);
                
                warnings.add(warning);
                
            } catch (Exception e) {
                logger.error("转换预警数据失败: {}", data, e);
            }
        }
        
        return warnings;
    }

    /**
     * 从Map中获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * 检测系统中可用的Python命令
     * 优先级: 1. 配置文件中的路径 2. python3 3. python 4. py
     */
    private String detectPythonCommand() {
        // 1. 首先检查配置文件中是否指定了Python路径
        if (configuredPythonPath != null && !configuredPythonPath.trim().isEmpty()) {
            String pythonPath = configuredPythonPath.trim();
            if (testPythonCommand(pythonPath)) {
                logger.info("使用配置的Python路径: {}", pythonPath);
                return pythonPath;
            } else {
                logger.warn("配置的Python路径无效: {}, 将尝试自动检测", pythonPath);
            }
        }
        
        // 2. 尝试常见的Python命令
        String[] commands = {"python3", "python", "py"};
        
        for (String cmd : commands) {
            if (testPythonCommand(cmd)) {
                logger.info("检测到Python命令: {}", cmd);
                return cmd;
            }
        }
        
        // 3. 尝试常见的Python安装路径（Windows）
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            String[] commonPaths = {
                "C:\\Python39\\python.exe",
                "C:\\Python310\\python.exe",
                "C:\\Python311\\python.exe",
                "C:\\Python312\\python.exe",
                "C:\\Program Files\\Python39\\python.exe",
                "C:\\Program Files\\Python310\\python.exe",
                "C:\\Program Files\\Python311\\python.exe",
                "C:\\Program Files\\Python312\\python.exe",
                "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Programs\\Python\\Python39\\python.exe",
                "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
                "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
                "C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Local\\Programs\\Python\\Python312\\python.exe"
            };
            
            for (String path : commonPaths) {
                if (testPythonCommand(path)) {
                    logger.info("在常见路径找到Python: {}", path);
                    return path;
                }
            }
        }
        
        logger.error("未找到可用的Python命令");
        return null;
    }
    
    /**
     * 测试Python命令是否可用
     */
    private boolean testPythonCommand(String command) {
        try {
            ProcessBuilder testBuilder = new ProcessBuilder(command, "--version");
            testBuilder.redirectErrorStream(true);
            Process testProcess = testBuilder.start();
            
            // 读取输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(testProcess.getInputStream(), "UTF-8"))) {
                String line = reader.readLine();
                if (line != null && line.contains("Python")) {
                    return true;
                }
            }
            
            int exitCode = testProcess.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        // 尝试多种日期格式
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        };
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，抛出异常
        throw new IllegalArgumentException("无法解析日期时间: " + dateTimeStr);
    }
}


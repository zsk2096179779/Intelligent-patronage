package com.fengqi.fund.fundadvisor.controller.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.FactorIcVisualizationResponse;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationRequest;
import com.fengqi.fund.fundadvisor.dto.factor.FactorValidationResponse;
import com.fengqi.fund.fundadvisor.dto.factor.LayeredBacktestResponse;
import com.fengqi.fund.fundadvisor.service.factor.FactorValidationService;
import com.fengqi.fund.fundadvisor.service.factor.LayeredBacktestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 因子检验控制器
 * 提供因子检验、IC/IR计算等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/factor/validation")
@RequiredArgsConstructor
@Tag(name = "因子检验", description = "因子检验和IC/IR计算相关接口")
public class FactorValidationController {
    
    private final FactorValidationService factorValidationService;
    private final LayeredBacktestService layeredBacktestService;
    
    @Operation(summary = "创建检验任务", description = "创建因子检验任务并异步执行IC/IR计算")
    @PostMapping("/tasks")
    public ResponseEntity<ResultDTO<FactorValidationResponse>> createValidationTask(
            @Valid @RequestBody FactorValidationRequest request) {
        log.info("创建检验任务，因子名称: {}, 开始日期: {}, 结束日期: {}", 
                request.getFactorNames(), request.getStartDate(), request.getEndDate());
        
        FactorValidationResponse response = factorValidationService.createValidationTask(request);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "查询任务状态", description = "查询检验任务的执行状态和进度")
    @GetMapping("/tasks/{taskId}/status")
    public ResponseEntity<ResultDTO<FactorValidationResponse>> getTaskStatus(
            @Parameter(description = "任务ID") @PathVariable Integer taskId) {
        FactorValidationResponse response = factorValidationService.getTaskStatus(taskId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "查询任务结果", description = "查询检验任务的IC/IR计算结果")
    @GetMapping("/tasks/{taskId}/results")
    public ResponseEntity<ResultDTO<FactorValidationResponse>> getTaskResults(
            @Parameter(description = "任务ID") @PathVariable Integer taskId) {
        FactorValidationResponse response = factorValidationService.getTaskResults(taskId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "查询所有任务", description = "查询所有检验任务列表")
    @GetMapping("/tasks")
    public ResponseEntity<ResultDTO<FactorValidationResponse>> getAllTasks() {
        FactorValidationResponse response = factorValidationService.getAllTasks();
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "查询因子历史结果", description = "查询指定因子的历史IC/IR计算结果")
    @GetMapping("/factors/{factorId}/history")
    public ResponseEntity<ResultDTO<FactorValidationResponse>> getFactorHistoryResults(
            @Parameter(description = "因子ID") @PathVariable Integer factorId,
            @Parameter(description = "返回条数限制") @RequestParam(required = false, defaultValue = "10") Integer limit) {
        FactorValidationResponse response = factorValidationService.getFactorHistoryResults(factorId, limit);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    // ==================== 分层回测图表数据接口 ====================
    
    @Operation(summary = "获取分位数累计收益率曲线", description = "获取分层回测的分位数累计收益率曲线数据")
    @GetMapping("/tasks/{taskId}/layered/cumulative-return")
    public ResponseEntity<ResultDTO<LayeredBacktestResponse>> getCumulativeReturnCurve(
            @Parameter(description = "任务ID") @PathVariable Integer taskId) {
        log.info("获取分位数累计收益率曲线，任务ID: {}", taskId);
        
        LayeredBacktestResponse response = layeredBacktestService.getCumulativeReturnCurve(taskId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "执行分层回测", description = "执行指定任务的分层回测计算")
    @PostMapping("/tasks/{taskId}/layered/execute")
    public ResponseEntity<ResultDTO<String>> executeLayeredBacktest(
            @Parameter(description = "任务ID") @PathVariable Integer taskId) {
        log.info("执行分层回测，任务ID: {}", taskId);
        
        boolean success = layeredBacktestService.executeLayeredBacktest(taskId);
        
        if (success) {
            return ResponseEntity.ok(ResultDTO.success("分层回测执行成功", "分层回测计算已完成"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error("分层回测执行失败，请检查任务状态和日志"));
        }
    }
    
    // ==================== IC检验可视化数据接口 ====================
    
    @Operation(summary = "获取分位数累计收益率曲线", description = "获取IC检验任务的分位数累计收益率曲线数据，用于绘制分位数累计收益率曲线图")
    @GetMapping("/tasks/{taskId}/ic/quantile-cumulative-return")
    public ResponseEntity<ResultDTO<FactorIcVisualizationResponse>> getQuantileCumulativeReturn(
            @Parameter(description = "任务ID") @PathVariable Integer taskId,
            @Parameter(description = "因子ID（可选，如果不指定则使用任务中的第一个因子）") 
            @RequestParam(required = false) Integer factorId) {
        log.info("获取分位数累计收益率曲线，任务ID: {}, 因子ID: {}", taskId, factorId);
        
        FactorIcVisualizationResponse response = factorValidationService.getQuantileCumulativeReturn(taskId, factorId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "获取分位数平均年化收益率柱状图", description = "获取IC检验任务的分位数平均年化收益率柱状图数据，用于绘制分位数平均年化收益率柱状图")
    @GetMapping("/tasks/{taskId}/ic/quantile-annualized-return")
    public ResponseEntity<ResultDTO<FactorIcVisualizationResponse>> getQuantileAnnualizedReturn(
            @Parameter(description = "任务ID") @PathVariable Integer taskId,
            @Parameter(description = "因子ID（可选，如果不指定则使用任务中的第一个因子）") 
            @RequestParam(required = false) Integer factorId) {
        log.info("获取分位数平均年化收益率柱状图，任务ID: {}, 因子ID: {}", taskId, factorId);
        
        FactorIcVisualizationResponse response = factorValidationService.getQuantileAnnualizedReturn(taskId, factorId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
    
    @Operation(summary = "获取IC检验统计图表数据（Alphalens风格）", 
            description = "获取IC检验的统计图表数据，包括IC序列图、IC分布直方图、IC统计汇总表和IC滚动统计，类似Alphalens的tears.create_information_tear_sheet()")
    @GetMapping("/tasks/{taskId}/ic/tear-sheet")
    public ResponseEntity<ResultDTO<FactorIcVisualizationResponse>> getIcTearSheetData(
            @Parameter(description = "任务ID") @PathVariable Integer taskId,
            @Parameter(description = "因子ID（可选，如果不指定则使用任务中的第一个因子）") 
            @RequestParam(required = false) Integer factorId) {
        log.info("获取IC检验统计图表数据，任务ID: {}, 因子ID: {}", taskId, factorId);
        
        FactorIcVisualizationResponse response = factorValidationService.getIcTearSheetData(taskId, factorId);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(ResultDTO.success(response, response.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResultDTO.error(response.getMessage()));
        }
    }
}


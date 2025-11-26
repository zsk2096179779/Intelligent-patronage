package com.fengqi.fund.fundadvisor.controller.factor;

import com.fengqi.fund.fundadvisor.dto.ResultDTO;
import com.fengqi.fund.fundadvisor.dto.factor.AddNodeRequest;
import com.fengqi.fund.fundadvisor.dto.factor.CreateTreeRequest;
import com.fengqi.fund.fundadvisor.dto.factor.MoveNodeRequest;
import com.fengqi.fund.fundadvisor.dto.factor.UpdateNodeRequest;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTree;
import com.fengqi.fund.fundadvisor.entity.factor.FactorTreeScene;
import com.fengqi.fund.fundadvisor.service.factor.FactorTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/factor/factor-trees")
@Tag(name = "因子树管理", description = "因子树的创建、查询、修改等操作")
public class FactorTreeController {
    
    @Autowired
    private FactorTreeService factorTreeService;
    
    @PostMapping
    @Operation(summary = "创建因子树", description = "创建一个新的因子树")
    public ResultDTO<FactorTree> createTree(@RequestBody CreateTreeRequest request) {
        log.info("创建因子树请求: {}", request);
        return factorTreeService.createFactorTree(
                request.getTreeName(),
                request.getDescription(),
                request.getSceneId()
        );
    }
    
    @GetMapping
    @Operation(summary = "获取所有因子树", description = "获取指定场景下的所有因子树列表")
    public ResultDTO<List<FactorTree>> getAllTrees(
            @Parameter(description = "场景ID，可选")
            @RequestParam(required = false) String sceneId) {
        log.info("获取因子树列表，场景ID: {}", sceneId);
        return factorTreeService.getAllTrees(sceneId);
    }
    
    @GetMapping("/{treeId}/structure")
    @Operation(summary = "获取树结构", description = "获取指定因子树的完整树形结构")
    public ResultDTO<Map<String, Object>> getTreeStructure(
            @Parameter(description = "因子树ID")
            @PathVariable Integer treeId) {
        log.info("获取树结构，树ID: {}", treeId);
        return factorTreeService.getTreeStructure(treeId);
    }
    
    @GetMapping("/nodes/{parentId}/children")
    @Operation(summary = "懒加载获取子节点", description = "懒加载方式获取指定节点的直接子节点")
    public ResultDTO<List<FactorTree>> getLazyChildNodes(
            @Parameter(description = "父节点ID")
            @PathVariable Integer parentId,
            @Parameter(description = "页大小，默认20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "偏移量，用于分页")
            @RequestParam(defaultValue = "0") Integer offset) {
        log.info("懒加载获取子节点，父节点ID: {}, 页大小: {}, 偏移量: {}", parentId, pageSize, offset);
        return factorTreeService.getLazyChildNodes(parentId);
    }
    
    @GetMapping("/nodes/{nodeId}/descendants")
    @Operation(summary = "获取所有后代节点", description = "获取指定节点及其所有后代节点")
    public ResultDTO<List<FactorTree>> getAllDescendants(
            @Parameter(description = "节点ID")
            @PathVariable Integer nodeId) {
        log.info("获取所有后代节点，节点ID: {}", nodeId);
        return factorTreeService.getAllDescendants(nodeId);
    }
    
    @GetMapping("/nodes/{nodeId}/path")
    @Operation(summary = "获取节点路径", description = "获取从根节点到指定节点的完整路径")
    public ResultDTO<List<Map<String, Object>>> getNodePath(
            @Parameter(description = "节点ID")
            @PathVariable Integer nodeId) {
        log.info("获取节点路径，节点ID: {}", nodeId);
        return factorTreeService.getNodePath(nodeId);
    }
    
    @GetMapping("/{treeId}/structure/lazy")
    @Operation(summary = "分页获取树结构", description = "支持懒加载和分页的树结构获取")
    public ResultDTO<Map<String, Object>> getTreeWithPagination(
            @Parameter(description = "因子树ID")
            @PathVariable Integer treeId,
            @Parameter(description = "页大小，默认20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "游标，用于分页")
            @RequestParam(required = false) Integer cursor) {
        log.info("分页获取树结构，树ID: {}, 页大小: {}, 游标: {}", treeId, pageSize, cursor);
        return factorTreeService.getTreeWithPagination(treeId, pageSize, cursor);
    }
    
    @PostMapping("/{treeId}/nodes")
    @Operation(summary = "添加节点", description = "在指定因子树中添加新节点")
    public ResultDTO<FactorTree> addNode(
            @Parameter(description = "因子树ID")
            @PathVariable Integer treeId,
            @RequestBody AddNodeRequest request) {
        log.info("添加节点请求，树ID: {}, 请求: {}", treeId, request);
        return factorTreeService.addTreeNode(
                treeId,
                request.getParentId(),
                request.getNodeName(),
                request.getNodeType(),
                request.getFactorId(),
                request.getDescription()
        );
    }
    

    
    @PutMapping("/nodes/{nodeId}")
    @Operation(summary = "更新节点", description = "更新指定节点的信息")
    public ResultDTO<FactorTree> updateTreeNode(
            @Parameter(description = "节点ID")
            @PathVariable Integer nodeId,
            @RequestBody UpdateNodeRequest request) {
        log.info("更新节点，节点ID: {}, 请求: {}", nodeId, request);
        return factorTreeService.updateTreeNode(
                nodeId,
                request.getNodeName(),
                request.getDescription(),
                request.getSortOrder()
        );
    }
    
    @PutMapping("/nodes/move")
    @Operation(summary = "移动节点", description = "将节点移动到新的父节点下")
    public ResultDTO<Boolean> moveTreeNode(
            @RequestBody MoveNodeRequest request) {
        log.info("移动节点，请求: {}", request);
        return factorTreeService.moveTreeNode(
                request.getNodeId(),
                request.getNewParentId(),
                request.getSortOrder()
        );
    }
    
    @DeleteMapping("/nodes/{nodeId}")
    @Operation(summary = "删除空节点", description = "删除指定的空节点（无子节点）")
    public ResultDTO<Boolean> deleteEmptyNode(
            @Parameter(description = "节点ID")
            @PathVariable Integer nodeId) {
        log.info("删除空节点，节点ID: {}", nodeId);
        return factorTreeService.deleteEmptyNode(nodeId);
    }
    
    @GetMapping("/search/trees")
    @Operation(summary = "搜索因子树", description = "根据关键词搜索因子树")
    public ResultDTO<List<FactorTree>> searchTrees(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword) {
        log.info("搜索因子树，关键词: {}", keyword);
        return factorTreeService.searchTrees(keyword);
    }
    
    @GetMapping("/search/factors")
    @Operation(summary = "搜索因子", description = "根据关键词搜索因子")
    public ResultDTO<List<FactorTree>> searchFactors(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword) {
        log.info("搜索因子，关键词: {}", keyword);
        return factorTreeService.searchFactors(keyword);
    }
    
    @GetMapping("/scenes")
    @Operation(summary = "获取所有场景", description = "获取所有可用的场景列表")
    public ResultDTO<List<FactorTreeScene>> getAllScenes() {
        log.info("获取所有场景列表");
        return factorTreeService.getAllScenes();
    }
    
    @PostMapping("/scenes")
    @Operation(summary = "创建场景", description = "创建新的场景")
    public ResultDTO<FactorTreeScene> createScene(
            @RequestParam String sceneId,
            @RequestParam String sceneName,
            @RequestParam(required = false) String sceneDesc) {
        log.info("创建场景，场景ID: {}, 场景名称: {}", sceneId, sceneName);
        return factorTreeService.createScene(sceneId, sceneName, sceneDesc);
    }
}
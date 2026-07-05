package com.airchina.crm.knowledge.controller;

import com.airchina.crm.common.result.Result;
import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.entity.KnowledgeCategory;
import com.airchina.crm.knowledge.service.KnowledgeImportService;
import com.airchina.crm.knowledge.service.KnowledgeService;
import com.airchina.crm.knowledge.vo.KnowledgeSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客服知识库接口
 */
@Slf4j
@Tag(name = "客服知识库", description = "知识搜索、分类查询、知识管理")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final KnowledgeImportService knowledgeImportService;

    @Operation(summary = "全文搜索", description = "根据关键词搜索知识库（支持中文分词、高亮显示）")
    @GetMapping("/search")
    public Result<KnowledgeSearchResult> search(
            @Parameter(description = "搜索关键词", example = "退改签")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "分类筛选", example = "TICKET")
            @RequestParam(required = false) String category,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(knowledgeService.search(keyword, category, page, size));
    }

    @Operation(summary = "知识详情", description = "根据ID查询知识详情（自动增加浏览量）")
    @GetMapping("/{id}")
    public Result<KnowledgeArticle> getById(
            @Parameter(description = "知识ID", required = true)
            @PathVariable String id) {
        // 增加浏览量
        knowledgeService.incrementViewCount(id);
        return Result.ok(knowledgeService.getById(id));
    }

    @Operation(summary = "按分类查询", description = "根据分类查询知识列表")
    @GetMapping("/category/{category}")
    public Result<List<KnowledgeArticle>> listByCategory(
            @Parameter(description = "分类代码", required = true, example = "FAQ")
            @PathVariable String category) {
        return Result.ok(knowledgeService.listByCategory(category));
    }

    @Operation(summary = "热门知识", description = "按浏览量排序的热门知识")
    @GetMapping("/hot")
    public Result<List<KnowledgeArticle>> listHot(
            @Parameter(description = "返回条数", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(knowledgeService.listHot(limit));
    }

    @Operation(summary = "分类列表", description = "获取所有知识分类")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> listCategories() {
        List<CategoryVO> categories = Arrays.stream(KnowledgeCategory.values())
                .map(c -> new CategoryVO(c.getCode(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());
        return Result.ok(categories);
    }

    @Operation(summary = "创建知识", description = "新增知识条目（管理员）")
    @PostMapping
    public Result<KnowledgeArticle> create(@RequestBody KnowledgeArticle article) {
        return Result.ok(knowledgeService.create(article));
    }

    @Operation(summary = "更新知识", description = "修改知识条目（管理员）")
    @PutMapping("/{id}")
    public Result<KnowledgeArticle> update(
            @PathVariable String id,
            @RequestBody KnowledgeArticle article) {
        return Result.ok(knowledgeService.update(id, article));
    }

    @Operation(summary = "删除知识", description = "删除知识条目（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "Excel批量导入", description = "上传Excel文件批量导入知识（按标题去重）")
    @PostMapping("/import")
    public Result<ImportResult> importFromExcel(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否覆盖已有数据")
            @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            int count = knowledgeImportService.importFromExcel(file, overwrite);
            return Result.ok(new ImportResult(count, "导入成功"));
        } catch (Exception e) {
            log.error("Excel导入失败", e);
            return Result.ok(new ImportResult(0, "导入失败: " + e.getMessage()));
        }
    }

    /**
     * 分类 VO
     */
    public static class CategoryVO {
        public String code;
        public String name;
        public String description;

        public CategoryVO(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        public int count;
        public String message;

        public ImportResult(int count, String message) {
            this.count = count;
            this.message = message;
        }
    }
}

package com.airchina.crm.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识搜索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识搜索结果")
public class KnowledgeSearchResult {

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "结果列表")
    private List<SearchItem> items;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "搜索结果项")
    public static class SearchItem {

        @Schema(description = "知识ID")
        private String id;

        @Schema(description = "标题（含高亮）")
        private String title;

        @Schema(description = "内容摘要（含高亮）")
        private String content;

        @Schema(description = "分类")
        private String category;

        @Schema(description = "标签")
        private List<String> tags;

        @Schema(description = "优先级")
        private Integer priority;

        @Schema(description = "浏览量")
        private Integer viewCount;
    }
}

package com.airchina.crm.knowledge.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库文章 - ES 文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "crm_knowledge")
public class KnowledgeArticle {

    @Id
    private String id;

    /** 标题 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 内容 */
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String content;

    /** 分类：PLATFORM/TICKET/FAQ/SERVICE/BENEFITS/SCRIPT/SYSTEM */
    @Field(type = FieldType.Keyword)
    private String category;

    /** 标签列表 */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /** 关键词（用于搜索权重） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String keywords;

    /** 优先级（越大越靠前） */
    @Field(type = FieldType.Integer)
    private Integer priority;

    /** 浏览次数 */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /** 状态：1启用 0禁用 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 创建时间 */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 创建人 */
    @Field(type = FieldType.Keyword)
    private String createBy;
}

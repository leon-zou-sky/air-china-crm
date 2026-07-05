package com.airchina.crm.knowledge.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 知识库 Excel 导入模板
 *
 * 运营人员按此格式整理数据
 */
@Data
public class KnowledgeExcelDTO {

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("标签")
    private String tags;

    @ExcelProperty("关键词")
    private String keywords;

    @ExcelProperty("优先级")
    private Integer priority;
}

package com.airchina.crm.knowledge.init;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.airchina.crm.knowledge.dto.KnowledgeExcelDTO;
import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.repository.KnowledgeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel 导入脚本
 *
 * 使用方式：
 * 1. 修改 Excel 文件路径
 * 2. 运行应用，自动导入
 *
 * 或者单独运行：
 * mvn spring-boot:run -Dspring-boot.run.arguments="--import.file=/path/to/file.xlsx"
 */
@Slf4j
@Component
public class ExcelImportScript implements CommandLineRunner {

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    // Excel 文件路径（修改这里）
    private static final String EXCEL_PATH = "data/knowledge.xlsx";

    @Override
    public void run(String... args) throws Exception {
        // 检查是否指定了导入文件
        String filePath = EXCEL_PATH;
        for (String arg : args) {
            if (arg.startsWith("--import.file=")) {
                filePath = arg.substring("--import.file=".length());
            }
        }

        // 检查文件是否存在
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            log.info("Excel文件不存在: {}，跳过导入", filePath);
            return;
        }

        log.info("开始导入Excel: {}", filePath);
        importFromFile(file);
    }

    /**
     * 从文件导入
     */
    public void importFromFile(java.io.File file) throws Exception {
        List<KnowledgeArticle> articles = new ArrayList<>();

        // 1. 读取 Excel
        EasyExcel.read(file, KnowledgeExcelDTO.class, new ReadListener<KnowledgeExcelDTO>() {
            @Override
            public void invoke(KnowledgeExcelDTO dto, AnalysisContext context) {
                KnowledgeArticle article = convertToArticle(dto);
                if (article != null) {
                    articles.add(article);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("Excel读取完成，共 {} 条", articles.size());
            }
        }).sheet().doRead();

        // 2. 批量写入 ES
        if (!articles.isEmpty()) {
            knowledgeRepository.saveAll(articles);
            log.info("成功导入 {} 条知识到ES", articles.size());
        } else {
            log.info("没有数据需要导入");
        }
    }

    /**
     * 转换为ES文档
     */
    private KnowledgeArticle convertToArticle(KnowledgeExcelDTO dto) {
        if (StringUtils.isBlank(dto.getTitle()) || StringUtils.isBlank(dto.getContent())) {
            return null;
        }

        String category = StringUtils.isNotBlank(dto.getCategory()) ?
                dto.getCategory().toUpperCase() : "FAQ";

        List<String> tags = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getTags())) {
            tags = Arrays.stream(dto.getTags().split("[,，]"))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }

        return KnowledgeArticle.builder()
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .category(category)
                .tags(tags)
                .keywords(dto.getKeywords())
                .priority(dto.getPriority() != null ? dto.getPriority() : 5)
                .viewCount(0)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createBy("excel-import")
                .build();
    }
}

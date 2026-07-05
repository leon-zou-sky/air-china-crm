package com.airchina.crm.knowledge.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.airchina.crm.knowledge.dto.KnowledgeExcelDTO;
import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.repository.KnowledgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库 Excel 导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeImportService {

    private final KnowledgeRepository knowledgeRepository;

    /**
     * 从 Excel 导入知识
     *
     * @param file Excel文件
     * @param overwrite 是否覆盖已有数据（按标题匹配）
     * @return 导入数量
     */
    public int importFromExcel(MultipartFile file, boolean overwrite) throws IOException {
        List<KnowledgeArticle> articles = new ArrayList<>();

        // 1. 读取 Excel
        EasyExcel.read(file.getInputStream(), KnowledgeExcelDTO.class, new ReadListener<KnowledgeExcelDTO>() {
            @Override
            public void invoke(KnowledgeExcelDTO dto, AnalysisContext context) {
                // 2. 转换为 ES 文档
                KnowledgeArticle article = convertToArticle(dto);
                if (article != null) {
                    articles.add(article);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.info("Excel 读取完成，共 {} 条数据", articles.size());
            }
        }).sheet().doRead();

        // 3. 批量写入 ES
        if (!articles.isEmpty()) {
            if (overwrite) {
                // 按标题删除已有数据
                articles.forEach(article -> {
                    List<KnowledgeArticle> existing = knowledgeRepository.findByTitle(article.getTitle());
                    existing.forEach(e -> knowledgeRepository.deleteById(e.getId()));
                });
            }
            knowledgeRepository.saveAll(articles);
            log.info("成功导入 {} 条知识到 ES", articles.size());
        }

        return articles.size();
    }

    /**
     * 转换 Excel DTO 为 ES 文档
     */
    private KnowledgeArticle convertToArticle(KnowledgeExcelDTO dto) {
        // 校验必填字段
        if (StringUtils.isBlank(dto.getTitle()) || StringUtils.isBlank(dto.getContent())) {
            log.warn("跳过无效数据：title={}, content={}", dto.getTitle(), dto.getContent());
            return null;
        }

        // 校验分类
        String category = dto.getCategory();
        if (StringUtils.isBlank(category)) {
            category = "FAQ"; // 默认分类
        }
        category = category.toUpperCase();

        // 解析标签
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
                .createBy("import")
                .build();
    }
}

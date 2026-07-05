package com.airchina.crm.knowledge.service.impl;

import com.airchina.crm.common.exception.BizException;
import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.repository.KnowledgeRepository;
import com.airchina.crm.knowledge.service.KnowledgeService;
import com.airchina.crm.knowledge.vo.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;

    private static final String INDEX_NAME = "crm_knowledge";

    @Override
    public KnowledgeSearchResult search(String keyword, String category, int page, int size) {
        // 构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        if (StringUtils.isNotBlank(keyword)) {
            // 多字段搜索，权重：标题 > 关键词 > 内容
            MultiMatchQueryBuilder multiMatch = QueryBuilders.multiMatchQuery(keyword)
                    .field("title", 3.0f)
                    .field("keywords", 2.0f)
                    .field("content", 1.0f)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .fuzziness("AUTO");
            queryBuilder.withQuery(multiMatch);
        } else {
            queryBuilder.withQuery(QueryBuilders.matchAllQuery());
        }

        // 分类过滤
        if (StringUtils.isNotBlank(category)) {
            queryBuilder.withFilter(QueryBuilders.termQuery("category", category));
        }

        // 只查询启用状态
        queryBuilder.withFilter(QueryBuilders.termQuery("status", 1));

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title").preTags("<em>").postTags("</em>")
                .field("content").preTags("<em>").postTags("</em>")
                .field("keywords").preTags("<em>").postTags("</em>")
                .numOfFragments(3).fragmentSize(150);
        queryBuilder.withHighlightBuilder(highlightBuilder);

        // 分页
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 排序：优先级降序，浏览量降序
        queryBuilder.withSort(Sort.by(Sort.Order.desc("priority"), Sort.Order.desc("viewCount")));

        NativeSearchQuery query = queryBuilder.build();

        // 执行查询
        SearchHits<KnowledgeArticle> searchHits = elasticsearchTemplate.search(
                query, KnowledgeArticle.class, IndexCoordinates.of(INDEX_NAME));

        // 转换结果
        List<KnowledgeSearchResult.SearchItem> items = searchHits.getSearchHits().stream()
                .map(hit -> {
                    KnowledgeArticle article = hit.getContent();
                    // 使用高亮内容
                    String title = getHighlightField(hit, "title", article.getTitle());
                    String content = getHighlightField(hit, "content", article.getContent());

                    return KnowledgeSearchResult.SearchItem.builder()
                            .id(article.getId())
                            .title(title)
                            .content(content)
                            .category(article.getCategory())
                            .tags(article.getTags())
                            .priority(article.getPriority())
                            .viewCount(article.getViewCount())
                            .build();
                })
                .collect(Collectors.toList());

        return KnowledgeSearchResult.builder()
                .total(searchHits.getTotalHits())
                .items(items)
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public KnowledgeArticle getById(String id) {
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new BizException("知识不存在"));
    }

    @Override
    public List<KnowledgeArticle> listByCategory(String category) {
        return knowledgeRepository.findByCategoryAndStatusOrderByPriorityDesc(category, 1);
    }

    @Override
    public List<KnowledgeArticle> listHot(int limit) {
        Page<KnowledgeArticle> page = knowledgeRepository.findByStatusOrderByViewCountDesc(1,
                PageRequest.of(0, limit));
        return page.getContent();
    }

    @Override
    public KnowledgeArticle create(KnowledgeArticle article) {
        article.setStatus(1);
        article.setViewCount(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        return knowledgeRepository.save(article);
    }

    @Override
    public KnowledgeArticle update(String id, KnowledgeArticle article) {
        KnowledgeArticle existing = getById(id);
        existing.setTitle(article.getTitle());
        existing.setContent(article.getContent());
        existing.setCategory(article.getCategory());
        existing.setTags(article.getTags());
        existing.setKeywords(article.getKeywords());
        existing.setPriority(article.getPriority());
        existing.setUpdateTime(LocalDateTime.now());
        return knowledgeRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        knowledgeRepository.deleteById(id);
    }

    @Override
    public void incrementViewCount(String id) {
        KnowledgeArticle article = getById(id);
        article.setViewCount(article.getViewCount() + 1);
        knowledgeRepository.save(article);
    }

    /**
     * 获取高亮字段
     */
    private String getHighlightField(SearchHit<KnowledgeArticle> hit, String field, String defaultValue) {
        List<String> highlights = hit.getHighlightFields().get(field);
        if (highlights != null && !highlights.isEmpty()) {
            return String.join("...", highlights);
        }
        return defaultValue;
    }
}

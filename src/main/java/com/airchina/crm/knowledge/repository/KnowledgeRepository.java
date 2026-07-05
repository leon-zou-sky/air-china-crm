package com.airchina.crm.knowledge.repository;

import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库 ES Repository
 */
@Repository
public interface KnowledgeRepository extends ElasticsearchRepository<KnowledgeArticle, String> {

    /**
     * 根据分类查询
     */
    List<KnowledgeArticle> findByCategoryAndStatus(String category, Integer status);

    /**
     * 根据分类查询，按优先级排序
     */
    List<KnowledgeArticle> findByCategoryAndStatusOrderByPriorityDesc(String category, Integer status);

    /**
     * 按浏览量排序（分页）
     */
    Page<KnowledgeArticle> findByStatusOrderByViewCountDesc(Integer status, Pageable pageable);

    /**
     * 根据标题查询（用于去重）
     */
    List<KnowledgeArticle> findByTitle(String title);
}

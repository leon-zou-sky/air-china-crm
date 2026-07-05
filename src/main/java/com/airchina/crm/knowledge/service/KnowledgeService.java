package com.airchina.crm.knowledge.service;

import com.airchina.crm.knowledge.entity.KnowledgeArticle;
import com.airchina.crm.knowledge.vo.KnowledgeSearchResult;

import java.util.List;

/**
 * 知识库服务接口
 */
public interface KnowledgeService {

    /**
     * 全文搜索
     *
     * @param keyword 搜索关键词
     * @param category 分类（可选）
     * @param page 页码
     * @param size 每页条数
     * @return 搜索结果
     */
    KnowledgeSearchResult search(String keyword, String category, int page, int size);

    /**
     * 根据ID查询
     */
    KnowledgeArticle getById(String id);

    /**
     * 根据分类查询
     */
    List<KnowledgeArticle> listByCategory(String category);

    /**
     * 热门知识（按浏览量）
     */
    List<KnowledgeArticle> listHot(int limit);

    /**
     * 创建知识
     */
    KnowledgeArticle create(KnowledgeArticle article);

    /**
     * 更新知识
     */
    KnowledgeArticle update(String id, KnowledgeArticle article);

    /**
     * 删除知识
     */
    void delete(String id);

    /**
     * 增加浏览量
     */
    void incrementViewCount(String id);
}

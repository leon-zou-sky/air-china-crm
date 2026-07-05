#!/bin/bash

# ES 数据查看脚本
#
# 使用方式：
# ./scripts/es-query.sh [命令]
#
# 命令：
#   stats    - 查看统计信息
#   list     - 列出所有知识
#   search   - 搜索知识
#   category - 按分类查看
#   delete   - 删除索引

ES_HOST="http://localhost:9200"
INDEX="crm_knowledge"

case "${1:-stats}" in
    stats)
        echo "=== ES 知识库统计 ==="
        echo ""
        echo "📊 文档总数:"
        curl -s "$ES_HOST/$INDEX/_count" | python3 -c "import json,sys; print(json.load(sys.stdin)['count'])"
        echo ""
        echo "📁 分类分布:"
        curl -s "$ES_HOST/$INDEX/_search" -H "Content-Type: application/json" -d '{
            "size": 0,
            "aggs": {
                "categories": {
                    "terms": {"field": "category", "size": 10}
                }
            }
        }' | python3 -c "
import json, sys
result = json.load(sys.stdin)
buckets = result['aggregations']['categories']['buckets']
for b in buckets:
    print(f\"  {b['key']}: {b['doc_count']}条\")
"
        ;;

    list)
        echo "=== 知识列表 ==="
        curl -s "$ES_HOST/$INDEX/_search?size=100" -H "Content-Type: application/json" -d '{
            "query": {"match_all": {}},
            "_source": ["title", "category", "tags", "viewCount"],
            "sort": [{"viewCount": "desc"}]
        }' | python3 -c "
import json, sys
result = json.load(sys.stdin)
for hit in result['hits']['hits']:
    src = hit['_source']
    tags = ', '.join(src.get('tags', []))
    print(f\"[{src['category']}] {src['title']} (浏览:{src.get('viewCount', 0)}) | 标签: {tags}\")
"
        ;;

    search)
        if [ -z "$2" ]; then
            echo "用法: $0 search <关键词>"
            exit 1
        fi
        echo "=== 搜索: $2 ==="
        curl -s "$ES_HOST/$INDEX/_search" -H "Content-Type: application/json" -d "{
            \"query\": {
                \"multi_match\": {
                    \"query\": \"$2\",
                    \"fields\": [\"title^3\", \"keywords^2\", \"content\"]
                }
            },
            \"highlight\": {
                \"fields\": {
                    \"title\": {\"pre_tags\": \"[\", \"post_tags\": \"]\"},
                    \"content\": {\"pre_tags\": \"[\", \"post_tags\": \"]\", \"fragment_size\": 100}
                }
            }
        }" | python3 -c "
import json, sys
result = json.load(sys.stdin)
print(f\"找到 {result['hits']['total']['value']} 条结果:\")
print()
for hit in result['hits']['hits']:
    src = hit['_source']
    hl = hit.get('highlight', {})
    title = hl.get('title', [src['title']])[0]
    content = hl.get('content', [src['content'][:100]])[0]
    print(f\"标题: {title}\")
    print(f\"分类: {src['category']}\")
    print(f\"内容: {content}...\")
    print('---')
"
        ;;

    category)
        if [ -z "$2" ]; then
            echo "用法: $0 category <分类代码>"
            echo "分类: PLATFORM/TICKET/FAQ/SERVICE/BENEFITS/SCRIPT/SYSTEM"
            exit 1
        fi
        echo "=== 分类: $2 ==="
        curl -s "$ES_HOST/$INDEX/_search" -H "Content-Type: application/json" -d "{
            \"query\": {
                \"bool\": {
                    \"must\": [
                        {\"term\": {\"category\": \"$2\"}},
                        {\"term\": {\"status\": 1}}
                    ]
                }
            },
            \"sort\": [{\"priority\": \"desc\"}],
            \"_source\": [\"title\", \"tags\", \"viewCount\", \"priority\"]
        }" | python3 -c "
import json, sys
result = json.load(sys.stdin)
print(f\"共 {result['hits']['total']['value']} 条:\")
for hit in result['hits']['hits']:
    src = hit['_source']
    print(f\"  [{src['priority']}] {src['title']} (浏览:{src.get('viewCount', 0)})\")
"
        ;;

    delete)
        echo "⚠️  即将删除索引 $INDEX"
        read -p "确认删除? (y/N): " confirm
        if [ "$confirm" = "y" ]; then
            curl -s -X DELETE "$ES_HOST/$INDEX"
            echo "索引已删除"
        else
            echo "取消删除"
        fi
        ;;

    *)
        echo "ES 知识库查看工具"
        echo ""
        echo "用法: $0 <命令>"
        echo ""
        echo "命令:"
        echo "  stats    - 查看统计信息"
        echo "  list     - 列出所有知识"
        echo "  search   - 搜索知识"
        echo "  category - 按分类查看"
        echo "  delete   - 删除索引"
        ;;
esac

#!/bin/bash

# 知识库 Excel 导入脚本
#
# 使用方式：
# ./scripts/import-knowledge.sh [excel文件路径]
#
# 示例：
# ./scripts/import-knowledge.sh data/knowledge.xlsx
# ./scripts/import-knowledge.sh /Users/zouxuefei/Desktop/知识库.xlsx

# 默认文件路径
DEFAULT_FILE="data/knowledge.xlsx"
FILE_PATH=${1:-$DEFAULT_FILE}

# 检查文件是否存在
if [ ! -f "$FILE_PATH" ]; then
    echo "❌ 文件不存在: $FILE_PATH"
    echo ""
    echo "使用方式: $0 [excel文件路径]"
    echo "示例: $0 data/knowledge.xlsx"
    exit 1
fi

echo "📖 开始导入知识库..."
echo "📁 文件路径: $FILE_PATH"
echo ""

# 运行导入
cd /Users/zouxuefei/java_project/air-china-crm
mvn spring-boot:run -Dspring-boot.run.arguments="--import.file=$FILE_PATH" 2>&1 | grep -E "导入|成功|失败|ERROR"

echo ""
echo "✅ 导入完成！"

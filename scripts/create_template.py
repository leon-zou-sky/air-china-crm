#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
创建 Excel 模板文件

使用方式：
    python3 scripts/create_template.py [输出路径]

示例：
    python3 scripts/create_template.py data/knowledge.xlsx
"""

import sys
import os

try:
    import openpyxl
except ImportError:
    print("❌ 缺少依赖: openpyxl")
    print("   安装命令: pip3 install openpyxl")
    sys.exit(1)


def create_template(output_path):
    """创建 Excel 模板"""
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "知识库"

    # 表头
    headers = ["标题", "内容", "分类", "标签", "关键词", "优先级"]
    ws.append(headers)

    # 示例数据
    samples = [
        [
            "退改签收费标准（2024版）",
            "一、自愿退票\n1. 头等舱/F舱：起飞前免费，起飞后收取10%\n2. 公务舱/C舱：起飞前收取5%，起飞后收取20%\n3. 经济舱/Y舱：起飞前收取10%，起飞后收取30%\n\n二、自愿改签\n1. 头等舱：免费改签\n2. 公务舱：收取5%改签费\n3. 经济舱：收取10%改签费",
            "TICKET",
            "退票,改签,收费",
            "退票 改签 收费 标准 费用",
            10
        ],
        [
            "行李托运规定",
            "一、国内航班\n- 头等舱：免费托运40公斤\n- 公务舱：免费托运30公斤\n- 经济舱：免费托运20公斤\n\n二、国际航班\n- 头等舱：2件，每件32公斤\n- 公务舱：2件，每件32公斤\n- 经济舱：2件，每件23公斤",
            "TICKET",
            "行李,托运,重量",
            "行李 托运 重量 超重 规定",
            9
        ],
        [
            "如何查询里程余额",
            "查询里程余额有以下方式：\n\n1. APP查询（推荐）\n   打开凤凰知音APP → 我的账户 → 里程余额\n\n2. 官网查询\n   登录www.airchina.com → 凤凰知音 → 我的账户\n\n3. 电话查询\n   拨打4008-100-999 → 按2查询里程",
            "FAQ",
            "里程,查询,余额",
            "里程 查询 余额 积分",
            10
        ]
    ]

    for sample in samples:
        ws.append(sample)

    # 设置列宽
    ws.column_dimensions['A'].width = 30
    ws.column_dimensions['B'].width = 60
    ws.column_dimensions['C'].width = 15
    ws.column_dimensions['D'].width = 20
    ws.column_dimensions['E'].width = 30
    ws.column_dimensions['F'].width = 10

    # 保存
    wb.save(output_path)
    print(f"✅ 模板已创建: {output_path}")
    print(f"   包含 {len(samples)} 条示例数据")


def main():
    # 获取输出路径
    if len(sys.argv) < 2:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        project_dir = os.path.dirname(script_dir)
        output_path = os.path.join(project_dir, "data", "knowledge.xlsx")
    else:
        output_path = sys.argv[1]

    # 确保目录存在
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    create_template(output_path)


if __name__ == "__main__":
    main()

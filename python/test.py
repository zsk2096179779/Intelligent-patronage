# -*- coding: utf-8 -*-
import sys
import io
import akshare as ak
import pandas as pd
import json
import time
from datetime import datetime
from openai import OpenAI

# 设置标准输出为UTF-8编码，解决乱码问题
# 将stdout重定向为只输出JSON，stderr用于调试信息
if sys.stdout.encoding != 'utf-8':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
if sys.stderr.encoding != 'utf-8':
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# ================= 配置部分 =================
API_KEY = "sk-7a3601f265844e9f8da81e9ade356c14" 
BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
MODEL_NAME = "qwen-plus"  

# 初始化 OpenAI 客户端
client = OpenAI(api_key=API_KEY, base_url=BASE_URL)

# ================= 第一步：获取财经新闻 =================
def get_financial_news(limit=10):
    """获取财联社电报数据，取最新的 limit 条"""
    print(f"正在获取最新的 {limit} 条财联社电报...", file=sys.stderr)
    try:
        df = ak.stock_info_global_cls()
        
        # 确保数据按时间倒序排列（最新的在前）
        # 先按发布日期和发布时间排序
        if '发布日期' in df.columns and '发布时间' in df.columns:
            # 创建完整的时间列用于排序
            df['排序时间'] = pd.to_datetime(df['发布日期'].astype(str) + ' ' + df['发布时间'].astype(str), errors='coerce')
            # 按时间倒序排列（最新的在前）
            df = df.sort_values('排序时间', ascending=False, na_position='last')
            # 删除临时列
            df = df.drop('排序时间', axis=1)
        elif '发布日期' in df.columns:
            # 如果只有发布日期，按发布日期倒序
            df = df.sort_values('发布日期', ascending=False, na_position='last')
        
        # 取最新的 limit 条
        return df.head(limit)
    except Exception as e:
        print(f"AkShare 获取数据失败: {e}", file=sys.stderr)
        return pd.DataFrame()

# ================= 第二步：构造 AI 提示词 =================
def analyze_risks_with_ai(news_list):
    """
    将新闻列表发送给 AI 进行风险提取
    news_list 结构: [{'title': '...', 'content': '...', 'time': '...'}]
    """
    
    # 将新闻列表转换为字符串供 AI 阅读
    news_text_block = ""
    for idx, item in enumerate(news_list):
        news_text_block += f"【新闻ID: {idx}】\n时间: {item['time']}\n标题: {item['title']}\n内容: {item['content']}\n----------------\n"

    # System Prompt: 定义 AI 的角色和输出格式（JSON）
    system_prompt = """
    你是一个专业的金融风控助手。你的任务是阅读财经新闻，识别其中是否包含对基金策略（如股票、债券、行业）有潜在风险的预警信息。
    
    请遵循以下规则：
    1. 仅提取负面、风险、监管、违约、大幅下跌、利空类信息。如果是中性或利好消息，请忽略。
    2. 必须严格返回标准的 JSON 格式列表，不要包含任何 Markdown 标记（如 ```json）。  
    3. JSON 字段说明：  
       - "title": 新闻标题(10字以内)。
       - "risk_level": 风险等级，只能是 "high", "medium", "low" 之一。  
         (high: 立案调查、违约、退市、重大造假; medium: 监管函、业绩大幅下滑、减持; low: 短期震荡、行业轻微利空)  
       - "description": 简短的风险描述（不超过50字），总结核心风险点。  
       - "event_time": 新闻中的发布时间。  
       - "related_sector": 涉及的板块或行业（如：半导体、新能源、房地产），如果没有则填 "通用"。  
    
    如果没有发现任何风险，返回空列表 []。  
    """  

    user_prompt = f"""  
    请分析以下财经新闻，提取风险预警信息：  
    
    {news_text_block}  
    """  

    print("正在调用 AI 进行分析...", file=sys.stderr)  
    try:  
        response = client.chat.completions.create(  
            model=MODEL_NAME,  
            messages=[  
                {"role": "system", "content": system_prompt},  
                {"role": "user", "content": user_prompt},  
            ],  
            temperature=0.1, # 降低随机性，保证格式稳定  
            response_format={"type": "json_object"} # 强制 JSON 模式 (部分模型支持，Qwen通常需在Prompt强调)  
        )  
        
        result_text = response.choices[0].message.content  
        # 清理可能存在的 Markdown 标记  
        result_text = result_text.replace("```json", "").replace("```", "").strip()  
        
        # 解析 JSON  
        result_json = json.loads(result_text)  
        
        # 兼容处理：有时 AI 会把列表包在一个 key 里，如 {"warnings": [...]}  
        if isinstance(result_json, dict):  
            for key in result_json:  
                if isinstance(result_json[key], list):  
                    return result_json[key]  
            return [] # 没找到列表  
        elif isinstance(result_json, list):  
            return result_json  
            
    except json.JSONDecodeError:  
        print(f"AI 返回的格式不是合法的 JSON: {result_text}", file=sys.stderr)  
        return []  
    except Exception as e:  
        print(f"AI 调用出错: {e}", file=sys.stderr)  
        return []  

# ================= 主程序逻辑 =================  
if __name__ == "__main__":  
    # 1. 获取数据  
    df_news = get_financial_news(limit=10) # 一次分析10条  
    
    if not df_news.empty:  
        # 2. 数据预处理：转为 List[Dict] 格式方便传给 AI  
        news_items = []  
        for _, row in df_news.iterrows():  
            # 组合日期和时间  
            event_dt = f"{row['发布日期']} {row['发布时间']}"  
            news_items.append({  
                "title": row['标题'],  
                "content": row['内容'],  
                "time": event_dt  
            })  
        
        # 3. AI 分析  
        warnings = analyze_risks_with_ai(news_items)  
        
        # 4. 结果处理 - 只输出JSON到stdout，调试信息输出到stderr
        if warnings:  
            # 只输出JSON到stdout（后端需要解析）
            print(json.dumps(warnings, ensure_ascii=False))
            sys.stdout.flush()  # 确保立即输出
            # 调试信息输出到stderr
            print(f"✅ 成功提取 {len(warnings)} 条预警信息", file=sys.stderr)
        else:  
            # 如果没有预警，输出空数组到stdout
            print("[]")
            sys.stdout.flush()
            print("✅ 分析完成，这 10 条新闻中未发现高风险预警。", file=sys.stderr)
    else:  
        # 如果没有新闻数据，输出空数组到stdout
        print("[]")
        sys.stdout.flush()
        print("未获取到新闻数据。", file=sys.stderr)  
import akshare as ak
import pandas as pd
import numpy as np

# ==========================================
# 1. 数据获取与清洗模块
# ==========================================
def get_merged_fund_data(code):
    print(f"Step 1: 获取 [{code}] 单位净值...")
    df_unit = ak.fund_open_fund_info_em(symbol=code, indicator="单位净值走势")
    
    print(f"Step 2: 获取 [{code}] 累计净值...")
    df_acc = ak.fund_open_fund_info_em(symbol=code, indicator="累计净值走势")
    
    if df_unit.empty or df_acc.empty:
        raise ValueError("数据获取不完整")

    # --- 数据合并 ---
    # 两个表都有 '净值日期'，以此为基准合并
    # df_unit 列: ['净值日期', '单位净值', '日增长率', ...]
    # df_acc 列:  ['净值日期', '累计净值']
    
    df_merge = pd.merge(df_unit, df_acc, on='净值日期', how='inner')
    
    # 重命名与格式化
    rename_map = {
        '净值日期': 'date', 
        '单位净值': 'nav_unit', 
        '累计净值': 'nav_acc'
    }
    df_merge = df_merge.rename(columns=rename_map)
    df_merge['date'] = pd.to_datetime(df_merge['date'])
    df_merge = df_merge.set_index('date').sort_index()
    
    # 转数字
    df_merge['nav_unit'] = pd.to_numeric(df_merge['nav_unit'], errors='coerce')
    df_merge['nav_acc'] = pd.to_numeric(df_merge['nav_acc'], errors='coerce')
    
    # 计算日收益率 (如果没有现成的)
    df_merge['change_rate'] = df_merge['nav_unit'].pct_change()
    
    return df_merge

# ==========================================
# 2. 因子计算器 (包含全部6个因子)
# ==========================================
class FundFactorCalculator:
    def __init__(self, df):
        self.df = df.copy()

    def calculate_all(self):
        self.factor_std_20()           # 2. 波动率
        self.factor_momentum_20()      # 3. 动量
        self.factor_max_drawdown()     # 4. 最大回撤
        self.factor_sharpe_20()        # 5. 夏普
        self.factor_mean_reversion()   # 6. 均值回归
        self.factor_dividend_proxy()   # 1. 股息率 (重点新增)
        
        return self.df.dropna()

    def factor_std_20(self):
        self.df['volatility_20'] = self.df['change_rate'].rolling(20).std()

    def factor_momentum_20(self):
        self.df['momentum_20'] = self.df['nav_unit'].pct_change(20)

    def factor_max_drawdown(self):
        # 注意：计算回撤要用累计净值(nav_acc)更准确，因为它包含了分红的回补
        # 如果只用单位净值，分红那天会显示暴跌，导致回撤计算错误
        rolling_max = self.df['nav_acc'].expanding().max()
        self.df['drawdown'] = (self.df['nav_acc'] - rolling_max) / rolling_max

    def factor_sharpe_20(self):
        mean_20 = self.df['change_rate'].rolling(20).mean()
        std_20 = self.df['change_rate'].rolling(20).std()
        self.df['sharpe_20'] = mean_20 / std_20.replace(0, np.nan)

    def factor_mean_reversion(self):
        ma_20 = self.df['nav_unit'].rolling(20).mean()
        self.df['mean_reversion_20'] = (self.df['nav_unit'] - ma_20) / ma_20

    def factor_dividend_proxy(self):
        """
        计算累计分红贡献率
        公式：(累计净值 - 单位净值) / 单位净值
        意义：如果不算这一部分，很多老债基看起来收益很低。这个指标越高，说明“藏”在分红里的收益越多。
        """
        self.df['total_dividend'] = self.df['nav_acc'] - self.df['nav_unit']
        # 股息率因子：历史累计分红占比
        self.df['dividend_ratio'] = self.df['total_dividend'] / self.df['nav_unit']

# ==========================================
# 3. 执行测试
# ==========================================
if __name__ == "__main__":
    test_code = "000045" # 招商产业债 (出了名的分红多)
    
    try:
        # 1. 获取并合并数据
        df_data = get_merged_fund_data(test_code)
        
        # 2. 计算
        print("开始计算所有因子...")
        calc = FundFactorCalculator(df_data)
        df_result = calc.calculate_all()
        
        # 3. 展示结果
        cols = [
            'nav_unit',       # 单位净值
            'nav_acc',        # 累计净值 (用来验证分红)
            'total_dividend', # 算出累计分了多少钱
            'dividend_ratio', # 【因子1】股息回报率
            'volatility_20',  # 【因子2】波动率
            'momentum_20',    # 【因子3】动量
            'drawdown',       # 【因子4】最大回撤
            'sharpe_20'       # 【因子5】夏普
        ]
        
        pd.set_option('display.max_columns', None)
        pd.set_option('display.width', 1000)
        pd.set_option('display.float_format', lambda x: '%.4f' % x)

        print("\n" + "="*80)
        print(f"基金 {test_code} 最终计算结果 (最近5天)")
        print("="*80)
        print(df_result[cols].tail(5))
        
        # 验证一下分红逻辑
        last = df_result.iloc[-1]
        print("\n【数据验证】")
        print(f"当前单位净值: {last['nav_unit']}")
        print(f"当前累计净值: {last['nav_acc']}")
        print(f"-> 历史累计分红: {last['total_dividend']:.4f} 元/份")
        print(f"-> 相当于当前价格的 {last['dividend_ratio']:.2%} (这就是我们要的股息因子)")

    except Exception as e:
        print(f"出错: {e}")
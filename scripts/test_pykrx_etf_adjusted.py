#!/usr/bin/env python3
"""
pykrx ETF adjusted_close 지원 여부 테스트
"""

try:
    from pykrx import stock
    import pandas as pd

    print("=" * 80)
    print("pykrx ETF adjusted_close 지원 여부 테스트")
    print("=" * 80)

    # 테스트 1: get_market_ohlcv로 ETF 조회 (일반 주식 함수 사용)
    print("\n[테스트 1] stock.get_market_ohlcv()로 ETF 조회 (adjusted=True)")
    try:
        df = stock.get_market_ohlcv("20240101", "20240110", "069500", adjusted=True)
        print("✅ 성공!")
        print(df.head())
        print(f"\n컬럼: {list(df.columns)}")
    except Exception as e:
        print(f"❌ 실패: {e}")

    # 테스트 2: get_market_ohlcv로 ETF 조회 (adjusted=False)
    print("\n[테스트 2] stock.get_market_ohlcv()로 ETF 조회 (adjusted=False)")
    try:
        df = stock.get_market_ohlcv("20240101", "20240110", "069500", adjusted=False)
        print("✅ 성공!")
        print(df.head())
    except Exception as e:
        print(f"❌ 실패: {e}")

    # 테스트 3: get_etf_ohlcv_by_date로 ETF 조회 (ETF 전용 함수)
    print("\n[테스트 3] stock.get_etf_ohlcv_by_date()로 ETF 조회")
    try:
        df = stock.get_etf_ohlcv_by_date("20240101", "20240110", "069500")
        print("✅ 성공!")
        print(df.head())
        print(f"\n컬럼: {list(df.columns)}")
    except Exception as e:
        print(f"❌ 실패: {e}")

    # 테스트 4: 비교 - 일반 주식의 경우
    print("\n[테스트 4] stock.get_market_ohlcv()로 일반 주식 조회 (adjusted=True)")
    try:
        df = stock.get_market_ohlcv("20240101", "20240110", "005930", adjusted=True)
        print("✅ 성공!")
        print(df.head())
        print(f"\n컬럼: {list(df.columns)}")
    except Exception as e:
        print(f"❌ 실패: {e}")

    print("\n" + "=" * 80)
    print("결론:")
    print("=" * 80)
    print("1. get_market_ohlcv()가 ETF 티커(069500)도 처리 가능한가?")
    print("2. adjusted=True/False에 따라 결과가 다른가?")
    print("3. get_etf_ohlcv_by_date()의 결과와 차이가 있는가?")

except ImportError:
    print("❌ pykrx가 설치되어 있지 않습니다.")
    print("설치 명령: pip install pykrx")

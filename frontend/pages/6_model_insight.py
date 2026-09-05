import streamlit as st
import pandas as pd
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_get, REGIONS

st.title("모델 설명력")
st.caption("각 지역 모델이 예측할 때 어떤 요인을 가장 많이 참고하는지 보여줍니다 (XGBoost feature importance).")

region_label = st.selectbox("지역", list(REGIONS.keys()))

if st.button("조회"):
    try:
        data = api_get(f"/models/{REGIONS[region_label]}/feature-importance")
        if data:
            df = pd.DataFrame(data)
            df["importance"] = (df["importance"] * 100).round(1)
            df = df.rename(columns={"feature": "요인", "importance": "중요도(%)"})
            st.bar_chart(df.set_index("요인")["중요도(%)"])
            st.dataframe(df, use_container_width=True)
        else:
            st.info("데이터가 없습니다.")
    except Exception as e:
        st.error(f"조회 실패: {e}")

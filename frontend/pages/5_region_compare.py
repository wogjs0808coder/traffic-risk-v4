import streamlit as st
import pandas as pd
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_post, REGIONS

st.title("지역 비교")
st.caption("같은 조건(차종/연령대)일 때 지역별로 위험도가 어떻게 다른지 비교합니다.")

VEHICLE_TYPES = ["승용", "승합", "화물", "이륜", "자전거", "원동기",
                  "개인형이동수단(PM)", "건설기계", "농기계", "사륜오토바이(ATV)", "특수", "기타불명"]
AGE_GROUPS = ["20세 이하", "21-30세", "31-40세", "41-50세", "51-60세", "61-64세", "65세 이상", "기타불명"]

vehicle_type = st.selectbox(
    "차종", VEHICLE_TYPES,
    index=(VEHICLE_TYPES.index(st.session_state.get("vehicle_type")) if st.session_state.get("vehicle_type") in VEHICLE_TYPES else VEHICLE_TYPES.index("승용"))
)
age_group = st.selectbox(
    "연령대", AGE_GROUPS,
    index=(AGE_GROUPS.index(st.session_state.get("age_group")) if st.session_state.get("age_group") in AGE_GROUPS else AGE_GROUPS.index("31-40세"))
)

if st.button("6개 지역 비교하기"):
    results = []
    progress = st.progress(0, text="지역별 예측 중...")
    for i, (label, code) in enumerate(REGIONS.items()):
        try:
            detail = api_post("/predict/detail", {
                "region": code, "vehicleType": vehicle_type, "ageGroup": age_group
            })
            results.append({
                "지역": label,
                "예측 사고유형": detail["predictedType"],
                "신뢰도(%)": round(detail["confidence"] * 100, 1),
            })
        except Exception as e:
            results.append({"지역": label, "예측 사고유형": f"오류: {e}", "신뢰도(%)": 0})
        progress.progress((i + 1) / len(REGIONS))
    progress.empty()

    df = pd.DataFrame(results).sort_values("신뢰도(%)", ascending=False)
    st.bar_chart(df.set_index("지역")["신뢰도(%)"])
    st.dataframe(df, use_container_width=True)

    top = df.iloc[0]
    st.markdown(f"**{top['지역']}**의 신뢰도가 가장 높습니다 ({top['예측 사고유형']}, {top['신뢰도(%)']}%).")

import streamlit as st
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_post, REGIONS

st.title("사고유형 예측 (테스트용)")

VEHICLE_TYPES = ["승용", "승합", "화물", "이륜", "자전거", "원동기",
                  "개인형이동수단(PM)", "건설기계", "농기계", "사륜오토바이(ATV)", "특수", "기타불명"]
AGE_GROUPS = ["20세 이하", "21-30세", "31-40세", "41-50세", "51-60세", "61-64세", "65세 이상", "기타불명"]

if not st.session_state.get("user_id"):
    st.warning("프로필을 먼저 설정하면 차종/연령대가 자동으로 채워집니다.")

region_label = st.selectbox("지역", list(REGIONS.keys()))
vehicle_type = st.selectbox(
    "차종", VEHICLE_TYPES,
    index=(VEHICLE_TYPES.index(st.session_state.get("vehicle_type")) if st.session_state.get("vehicle_type") in VEHICLE_TYPES else VEHICLE_TYPES.index("승용"))
)
age_group = st.selectbox(
    "연령대", AGE_GROUPS,
    index=(AGE_GROUPS.index(st.session_state.get("age_group")) if st.session_state.get("age_group") in AGE_GROUPS else AGE_GROUPS.index("31-40세"))
)

st.caption("주야/날씨/노면상태/계절은 서버에서 현재 시각과 실시간 기상 정보로 자동 채워집니다.")

if st.button("예측하기"):
    payload = {"region": REGIONS[region_label], "vehicleType": vehicle_type, "ageGroup": age_group}
    if st.session_state.get("user_id"):
        payload["userId"] = st.session_state.user_id
    try:
        result = api_post("/predict/detail", payload)
        st.metric("예측된 사고유형", result["predictedType"])
        st.metric("신뢰도", f"{result['confidence'] * 100:.1f}%")

        st.caption(
            f"예측 시점 조건: {result.get('timeOfDay','-')} · {result.get('weather','-')} · "
            f"노면 {result.get('roadCondition','-')} · {result.get('season','-')} · "
            f"기온 {result.get('avgTemp',0):.1f}°C"
        )

        st.write("가능성 있는 사고유형 (상위 3개)")
        for tp in result.get("topTypes", []):
            st.write(f"{tp['type']} — {tp['probability']*100:.1f}%")
            st.progress(min(1.0, tp["probability"]))
    except Exception as e:
        st.error(f"예측 실패: {e}")

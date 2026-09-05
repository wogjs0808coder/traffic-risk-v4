import streamlit as st
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_post

st.title("프로필 설정")

VEHICLE_TYPES = ["승용", "승합", "화물", "이륜", "자전거", "원동기",
                  "개인형이동수단(PM)", "건설기계", "농기계", "사륜오토바이(ATV)", "특수", "기타불명"]
AGE_GROUPS = ["20세 이하", "21-30세", "31-40세", "41-50세", "51-60세", "61-64세", "65세 이상", "기타불명"]

vehicle_type = st.selectbox("차종", VEHICLE_TYPES)
age_group = st.selectbox("연령대", AGE_GROUPS)

if st.button("저장"):
    payload = {"vehicleType": vehicle_type, "ageGroup": age_group}
    if st.session_state.get("user_id"):
        payload["userId"] = st.session_state.user_id
    try:
        result = api_post("/profiles", payload)
        st.session_state.user_id = result["userId"]
        st.session_state.vehicle_type = result["vehicleType"]
        st.session_state.age_group = result["ageGroup"]
        st.success(f"저장 완료 (userId: {result['userId']})")
    except Exception as e:
        st.error(f"저장 실패: {e}")

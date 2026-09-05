import streamlit as st
import pandas as pd
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_get, REGIONS

st.title("예측 이력")

tab1, tab2, tab3 = st.tabs(["이력 목록", "통계", "내 경로 기록"])

with tab1:
    only_mine = st.checkbox("내 이력만 보기", value=False, disabled=not st.session_state.get("user_id"))
    region_label = st.selectbox("지역 필터", ["전체"] + list(REGIONS.keys()))
    page = st.number_input("페이지", min_value=0, value=0)

    params = {"page": page, "size": 10}
    if region_label != "전체":
        params["region"] = REGIONS[region_label]
    if only_mine and st.session_state.get("user_id"):
        params["userId"] = st.session_state.user_id

    try:
        data = api_get("/history", params=params)
        rows = data["content"]
        if rows:
            df = pd.DataFrame(rows)[["createdAt", "region", "predictedType", "confidence"]]
            st.dataframe(df, use_container_width=True)
            st.caption(f"전체 {data['totalElements']}건 중 {data['numberOfElements']}건 표시")
        else:
            st.info("이력이 없습니다.")
    except Exception as e:
        st.error(f"조회 실패: {e}")

with tab2:
    region_label = st.selectbox("지역 필터 (통계)", ["전체"] + list(REGIONS.keys()), key="stats_region")
    params = {}
    if region_label != "전체":
        params["region"] = REGIONS[region_label]

    try:
        stats = api_get("/history/stats", params=params)
        if stats:
            df = pd.DataFrame(stats, columns=["지역", "사고유형", "건수"])
            st.bar_chart(df.set_index("사고유형")["건수"])
            st.dataframe(df, use_container_width=True)
        else:
            st.info("통계 데이터가 없습니다.")
    except Exception as e:
        st.error(f"조회 실패: {e}")

with tab3:
    if not st.session_state.get("user_id"):
        st.info("프로필을 먼저 설정해야 내 경로 기록을 볼 수 있습니다.")
    else:
        try:
            data = api_get("/routes", params={"userId": st.session_state.user_id, "page": 0, "size": 10})
            rows = data["content"]
            if rows:
                df = pd.DataFrame(rows)[["createdAt", "originAddress", "destinationAddress"]]
                st.dataframe(df, use_container_width=True)
            else:
                st.info("저장된 경로 기록이 없습니다.")
        except Exception as e:
            st.error(f"조회 실패: {e}")

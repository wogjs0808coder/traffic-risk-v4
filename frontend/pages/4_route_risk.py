import streamlit as st
import folium
from streamlit_folium import st_folium
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common import api_post

st.title("경로 위험 예측")

REGION_KR = {
    "seoul": "서울", "busan": "부산", "daegu": "대구",
    "incheon": "인천", "daejeon": "대전", "yangsan": "양산",
}
RISK_COLOR = {"높음": "🔴", "보통": "🟡", "낮음": "🟢"}
VEHICLE_TYPES = ["승용", "승합", "화물", "이륜", "자전거", "원동기",
                  "개인형이동수단(PM)", "건설기계", "농기계", "사륜오토바이(ATV)", "특수", "기타불명"]
AGE_GROUPS = ["20세 이하", "21-30세", "31-40세", "41-50세", "51-60세", "61-64세", "65세 이상", "기타불명"]

col1, col2 = st.columns(2)
origin = col1.text_input("출발지", placeholder="예: 서울역")
destination = col2.text_input("목적지", placeholder="예: 강남역")

if "route_id" not in st.session_state:
    st.session_state.route_id = None
if "route_coords" not in st.session_state:
    st.session_state.route_coords = None
if "segments" not in st.session_state:
    st.session_state.segments = None

if st.button("경로 조회"):
    payload = {"originAddress": origin, "destinationAddress": destination}
    if st.session_state.get("user_id"):
        payload["userId"] = st.session_state.user_id
    try:
        result = api_post("/routes", payload)
        st.session_state.route_id = result["routeId"]
        st.session_state.route_coords = result["coordinates"]
        st.session_state.segments = None
        st.success(f"경로 조회 완료 ({len(result['coordinates'])}개 좌표)")
    except Exception as e:
        st.error(f"경로 조회 실패: {e}")

if st.session_state.route_coords:
    coords = st.session_state.route_coords
    center = coords[len(coords) // 2]
    fmap = folium.Map(location=center, zoom_start=13)
    folium.PolyLine(coords, color="#1a56db", weight=4).add_to(fmap)
    folium.Marker(coords[0], tooltip="출발", icon=folium.Icon(color="green")).add_to(fmap)
    folium.Marker(coords[-1], tooltip="도착", icon=folium.Icon(color="red")).add_to(fmap)
    st_folium(fmap, width=900, height=500)

    st.divider()
    st.subheader("구간별 위험도 분석")
    st.caption("경유 지역을 촘촘하게 판별하기 위해 조회에 10~15초 정도 걸릴 수 있습니다.")

    vehicle_type = st.selectbox("차종", VEHICLE_TYPES,
                                 index=(VEHICLE_TYPES.index(st.session_state.get("vehicle_type")) if st.session_state.get("vehicle_type") in VEHICLE_TYPES else VEHICLE_TYPES.index("승용")))
    age_group = st.selectbox("연령대", AGE_GROUPS,
                              index=(AGE_GROUPS.index(st.session_state.get("age_group")) if st.session_state.get("age_group") in AGE_GROUPS else AGE_GROUPS.index("31-40세")))

    if st.button("위험도 분석"):
        with st.spinner("경유 지역 판별 및 구간별 예측 중..."):
            try:
                st.session_state.segments = api_post(
                    f"/routes/{st.session_state.route_id}/risk",
                    {"vehicleType": vehicle_type, "ageGroup": age_group}
                )
            except Exception as e:
                st.error(f"위험도 분석 실패: {e}")

    if st.session_state.segments:
        segments = st.session_state.segments

        highest = max(segments, key=lambda s: s["confidence"])
        st.markdown(
            f"**경유 지역 {len(segments)}곳** 중 "
            f"**{REGION_KR.get(highest['region'], highest['region'])}** 구간이 가장 위험도가 높습니다 "
            f"({highest['riskLevel']}, {highest['predictedType']})."
        )

        for seg in segments:
            region_kr = REGION_KR.get(seg["region"], seg["region"])
            icon = RISK_COLOR.get(seg["riskLevel"], "⚪")
            with st.expander(f"{icon} {region_kr} — 위험도 {seg['riskLevel']} ({seg['predictedType']})"):
                st.write(
                    f"예측 시점 조건: {seg.get('timeOfDay', '-')} · {seg.get('weather', '-')} · "
                    f"노면 {seg.get('roadCondition', '-')} · {seg.get('season', '-')} · "
                    f"기온 {seg.get('avgTemp', 0):.1f}°C"
                )
                st.write("예상되는 사고 유형 (상위 3개)")
                for tp in seg.get("topTypes", []):
                    pct = tp["probability"] * 100
                    st.write(f"{tp['type']} — {pct:.1f}%")
                    st.progress(min(1.0, tp["probability"]))

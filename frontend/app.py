import streamlit as st

st.set_page_config(page_title="교통사고 위험 예측", layout="wide")

if "user_id" not in st.session_state:
    st.session_state.user_id = None
if "vehicle_type" not in st.session_state:
    st.session_state.vehicle_type = None
if "age_group" not in st.session_state:
    st.session_state.age_group = None

home = st.Page("home.py", title="홈", icon=":material/home:", default=True)
profile = st.Page("pages/1_profile.py", title="프로필", icon=":material/person:")
predict = st.Page("pages/2_predict.py", title="사고유형 예측", icon=":material/warning:")
history = st.Page("pages/3_history.py", title="예측 이력", icon=":material/history:")
route_risk = st.Page("pages/4_route_risk.py", title="경로 위험 예측", icon=":material/map:")
region_compare = st.Page("pages/5_region_compare.py", title="지역 비교", icon=":material/bar_chart:")
model_insight = st.Page("pages/6_model_insight.py", title="모델 설명력", icon=":material/insights:")

nav = st.navigation([home, profile, predict, history, route_risk, region_compare, model_insight])
nav.run()

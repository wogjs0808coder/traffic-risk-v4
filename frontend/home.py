import streamlit as st

st.title("교통사고 위험 예측 시스템")
st.write("왼쪽 메뉴에서 원하는 기능을 선택하세요.")

if st.session_state.get("user_id"):
    st.success(f"프로필 연결됨: {st.session_state.vehicle_type} / {st.session_state.age_group}")
else:
    st.info("프로필이 설정되지 않았습니다. '프로필' 메뉴에서 먼저 설정해주세요.")

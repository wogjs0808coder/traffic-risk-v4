import streamlit as st
import requests

try:
    API_BASE = st.secrets["API_BASE"]
except Exception:
    API_BASE = "http://localhost:8080/api"

REGIONS = {
    "서울": "seoul", "부산": "busan", "대구": "daegu",
    "인천": "incheon", "대전": "daejeon", "양산": "yangsan",
}


def api_post(path, payload):
    resp = requests.post(f"{API_BASE}{path}", json=payload, timeout=60)
    resp.raise_for_status()
    return resp.json()


def api_get(path, params=None):
    resp = requests.get(f"{API_BASE}{path}", params=params, timeout=30)
    resp.raise_for_status()
    return resp.json()

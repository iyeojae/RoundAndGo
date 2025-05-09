# ⛳ GolfTripMate - 골프 연계 관광 정보 플랫폼
> by **꾸러기 수비대**

> **디지털 골프 여행 코디네이터**  
> 골프 라운딩과 지역 관광을 한 번에!  
> 예약부터 경로 추천, 일정 공유까지 가능한 **개인화 골프 큐레이션 서비스**

---
## 🧑‍💻 팀 소개

> 본 프로젝트는 한서대학교 멋쟁이사자처럼 동아리 소속 팀 **꾸러기 수비대**가 개발하였습니다.  
> 언제나 꾸러기처럼 도전하고, 수비대처럼 문제를 해결합니다! 💪🦁
> 골프와 꾸러기 수비대의 연관 이스터에그를 찾으신분!?!?!?!
---

## ✅ 프로젝트 개요

- 골프장 중심의 **음식점, 숙소, 명소** 통합 정보 제공
- 예약 확인부터 라운딩 일정 구성, 공유, 히스토리 저장까지 지원
- 실용적이고 직관적인 **디지털 골프 여행 코디네이터 플랫폼**

---

## 🌟 제안 배경

골프는 2030~4060세대에 걸쳐 각광받는 **니치 스포츠**로,  
골프장을 중심으로 전국을 순회하며 라운딩을 즐기는 **체류형 관광** 문화가 정착되고 있습니다.

그러나 현재 관련 정보는 **분산**되어 있어 사용자 불편이 크고,  
단순 예약 외에 **관광 콘텐츠와 연계된 서비스는 매우 부족**합니다.

**👉 본 플랫폼은 골프 여행의 전 과정을 통합하여 사용자 편의를 높이고,  
지역 경제 활성화에도 기여하는 스마트 관광 플랫폼을 목표로 합니다.**

---

## 💡 주요 기능

| 번호 | 기능명 | 설명 |
| :--: | :----- | :--- |
| 0 | 필드 예약 유무 확인 | 외부 예약 링크 제공 (골프존, 카카오골프예약 등) |
| 0-1 | 오늘의 운세 | 골프 치기 좋은 날 가볍게 제안 |
| 1 | 라운딩 멤버 모집 | 친구 추가, 쪽지/카카오톡 ID 공유 기능 |
| 1-2 | 일정 공유 | 카카오 캘린더 API 연동 |
| 2 | 최적 경로 추천 | Kakao Navi API 활용, 숙소·관광지까지 안내 |
| 3 | 실시간 점수판 | 팀 스코어 기록, 랭킹 기능 |
| 4 | 날씨 정보 제공 | 기상청 날씨 API 활용 (기온, 바람 등) |
| 5 | 스탬프 로드 | 방문 기록 저장 및 추천 기반 |
| 6 | 금액대 필터 | 가성비 / 프리미엄 필터링 기능 |
| 7 | 주변 정보 추천 | TourAPI 4.0 활용, 음식점·숙소·관광지 추천 |

---

## 🔧 공공데이터/API 활용 계획

| 출처 | 활용 내용 |
| ---- | ---------- |
| [TourAPI 4.0](https://api.visitkorea.or.kr/#/) | 음식점, 숙소, 관광지 정보 통합 제공 |
| [전국 골프장 현황](https://www.data.go.kr/data/15118920/fileData.do) | 골프장 시설 정보 |
| [국토부 골프장현황도](https://www.vworld.kr/dev/v4dv_2ddataguide2_s002.do?svcIde=sgisgolf) | 지도 기반 시각화 |
| [기상청 날씨 API](https://www.data.go.kr/data/15043494/fileData.do) | 날씨 정보 기반 골프 적합성 판단 |
| [Kakao API](https://developers.kakao.com/) | 로그인, 친구 공유, 경로 안내 등 |

---

## 🧭 사용자 흐름

1. **카카오 로그인**
2. 원하는 지역 또는 골프장 선택
3. 주변 숙소·음식점·관광지 자동 추천
4. 라운딩 일정 저장 및 친구와 공유
5. 점수 기록 및 스탬프 저장
6. 히스토리 기반 개인 맞춤 추천 제공

---

## 🛠 기술 스택

<div align="center">

| 영역 | 기술 |
|:----:|:----:|
| 프론트엔드 | <img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB"/> |
| 백엔드 | <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"/> |
| 인증 | <img src="https://img.shields.io/badge/Kakao%20Login-FFCD00?style=for-the-badge&logo=kakao&logoColor=000000"/> |
| 지도 및 경로 | <img src="https://img.shields.io/badge/Kakao%20Map%20API-FFCD00?style=for-the-badge&logo=kakao&logoColor=000000"/> |
| 날씨 API | <img src="https://img.shields.io/badge/Weather%20API-007ACC?style=for-the-badge&logo=cloud&logoColor=white"/> |
| 배포(FE) | <img src="https://img.shields.io/badge/Netlify-00C7B7?style=for-the-badge&logo=netlify&logoColor=white"/> |
| 배포(BE) | <img src="https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white"/> <img src="https://img.shields.io/badge/Railway-000000?style=for-the-badge&logo=railway&logoColor=white"/> |

</div>

---

## 🎯 기대 효과

- **지역 체류형 관광** 활성화
- **정보 분산 해소**로 사용자 편의성 향상
- **숙박·음식점과의 연결 효과**로 지역경제 기여
- **히스토리 기반 맞춤 추천**으로 재방문 유도
- **고소득층 타겟팅**을 통한 실질적 소비 연결

---

## 📌 향후 계획

- 외국인 대상 다국어 버전 개발
- AI 기반 일정 자동 추천 기능
- 제휴 골프장/숙소 연결로 실질적 할인 제공

---

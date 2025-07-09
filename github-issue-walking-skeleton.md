# 🚀 Walking Skeleton: AI 기반 레시피 추천 및 구매 링크 제공 기본 기능 구현

## 📋 개요
Walking Skeleton 접근 방법에 따라 CookBot 앱의 핵심 기능인 AI 기반 레시피 추천 및 구매 링크 제공 기능의 최소 동작 버전을 구현합니다.

## 🎯 목표
사용자가 "단백질 60g을 채울 수 있는 6000원 미만 레시피 알려줘"와 같은 요청을 하면, 서버에서 이를 받아 AI를 통해 해당 요구사항을 충족하는 [레시피 + 구매 링크]를 응답하는 기능을 구현합니다.

## 🔄 현재 상황 vs 기대 결과
- **현재:** 빈 저장소 상태, 소스 코드 없음
- **기대:** 사용자 요청에 따른 AI 레시피 추천 + 구매 링크 제공 기능 동작

## 🛠 기술 스택
- **Backend:** Kotlin + Spring Boot
- **Architecture:** Hexagonal Architecture (포트와 어댑터 패턴)
- **AI Integration:** OpenAI API 또는 ChatGPT API
- **Shopping Integration:** 쿠팡, 마켓컬리 등 쇼핑몰 API

## 📋 구현 태스크
- [ ] 프로젝트 초기 설정 및 의존성 추가
- [ ] 헥사고날 아키텍처 기반 패키지 구조 생성
- [ ] AI 레시피 추천 API 설계 및 구현
- [ ] 구매 링크 생성 로직 구현
- [ ] REST API 엔드포인트 구현
- [ ] 통합 테스트 작성

## 🔍 성공 기준
1. POST /api/recipe-recommend 엔드포인트가 정상 동작
2. 사용자 요청 파라미터 처리 (단백질량, 가격 등)
3. AI API 연동을 통한 레시피 추천 응답
4. 추천 레시피의 재료별 구매 링크 제공
5. 모든 테스트 통과

## 📚 참고 문서
- [CLAUDE.md 헥사고날 아키텍처 지침](./CLAUDE.md#아키텍처-지침)
- [CLAUDE.md TDD 개발 지침](./CLAUDE.md#개발-지침)

## 🏷 라벨
- `enhancement`
- `high-priority`
- `walking-skeleton`
- `ai-integration`
- `backend`
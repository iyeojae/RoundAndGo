# 저장된 코스 API 명세서

## 기존 엔드포인트

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/courses/saved | 코스 저장 | Header: Authorization; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON |
| GET | /api/courses/saved/my | 내 저장된 코스 목록 | Header: Authorization | CommonResponse<List<SavedCourseResponseDto>> | Auth 필요 |
| GET | /api/courses/saved/public | 공개된 코스 목록 | - | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/public/type/{courseType} | 코스 타입별 공개 코스 조회 | Path: courseType | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/{courseId} | 저장된 코스 단건 조회 | Path: courseId | CommonResponse<SavedCourseResponseDto> | 공개 |
| PUT | /api/courses/saved/{courseId} | 저장된 코스 수정 | Header: Authorization; Path: courseId; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON |
| DELETE | /api/courses/saved/{courseId} | 저장된 코스 삭제 | Header: Authorization; Path: courseId | CommonResponse<Void> | Auth 필요 |

## 새로 추가된 엔드포인트 (추천 코스 기반)

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/courses/saved/from-recommendation | 추천 코스 ID로 코스 저장 | Header: Authorization; Body: SaveCourseFromRecommendationRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON, 스케줄 자동 반영 |
| PUT | /api/courses/saved/{courseId}/from-recommendation | 추천 코스 기반 코스 수정/생성 (Upsert) | Header: Authorization; Path: courseId; Body: SaveCourseFromRecommendationRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON, 코스 없으면 생성, 스케줄 자동 반영 |

## 전체 통합 API 명세서

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/courses/saved | 코스 저장 | Header: Authorization; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON |
| POST | /api/courses/saved/from-recommendation | 추천 코스 ID로 코스 저장 | Header: Authorization; Body: SaveCourseFromRecommendationRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON, 스케줄 자동 반영 |
| GET | /api/courses/saved/my | 내 저장된 코스 목록 | Header: Authorization | CommonResponse<List<SavedCourseResponseDto>> | Auth 필요 |
| GET | /api/courses/saved/public | 공개된 코스 목록 | - | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/public/type/{courseType} | 코스 타입별 공개 코스 조회 | Path: courseType | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/{courseId} | 저장된 코스 단건 조회 | Path: courseId | CommonResponse<SavedCourseResponseDto> | 공개 |
| PUT | /api/courses/saved/{courseId} | 저장된 코스 수정 | Header: Authorization; Path: courseId; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON |
| PUT | /api/courses/saved/{courseId}/from-recommendation | 추천 코스 기반 코스 수정/생성 (Upsert) | Header: Authorization; Path: courseId; Body: SaveCourseFromRecommendationRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, JSON, 코스 없으면 생성, 스케줄 자동 반영 |
| DELETE | /api/courses/saved/{courseId} | 저장된 코스 삭제 | Header: Authorization; Path: courseId | CommonResponse<Void> | Auth 필요 |

## 요청 DTO

### SavedCourseRequestDto
```json
{
  "courseName": "string",
  "description": "string",
  "courseType": "string",
  "startDate": "2024-01-01",
  "travelDays": 2,
  "isPublic": true,
  "courseDays": [
    {
      "dayNumber": 1,
      "courseDate": "2024-01-01",
      "teeOffTime": "09:00",
      "golfCourseId": 1,
      "places": [
        {
          "type": "restaurant",
          "name": "맛집 이름",
          "address": "주소",
          "imageUrl": "이미지 URL",
          "distanceKm": 5.5,
          "mapx": "127.123",
          "mapy": "37.456",
          "visitOrder": 1
        }
      ]
    }
  ]
}
```

### SaveCourseFromRecommendationRequestDto (새로 추가)
```json
{
  "courseName": "string",
  "description": "string", 
  "isPublic": true,
  "recommendationIds": [1, 2, 3]
}
```

## 주요 특징

### 새로 추가된 기능들
1. **추천 코스 기반 저장**: 여러 추천 코스 ID를 받아서 하나의 저장된 코스로 변환
2. **Upsert 로직**: 코스가 없으면 생성, 있으면 수정
3. **자동 스케줄 동기화**: 코스 저장/수정 시 개인 스케줄에 자동 반영
4. **변경사항 자동 반영**: 추천 코스의 내용이 변경되면 저장된 코스와 스케줄에도 반영

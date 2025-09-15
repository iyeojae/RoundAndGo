# RoundAndGo 프로젝트 전체 통합 API 명세서

## 📋 목차
1. [인증 (Auth) API](#1-인증-auth-api)
2. [커뮤니티 (Community) API](#2-커뮤니티-community-api)  
3. [댓글 (Comment) API](#3-댓글-comment-api)
4. [골프장 (GolfCourse) API](#4-골프장-golfcourse-api)
5. [코스 추천 (CourseRecommendation) API](#5-코스-추천-courserecommendation-api)
6. [저장된 코스 (SavedCourse) API](#6-저장된-코스-savedcourse-api)
7. [스케줄 (Schedule) API](#7-스케줄-schedule-api)
8. [프로필 이미지 (ProfileImage) API](#8-프로필-이미지-profileimage-api)
9. [관광 정보 (TourInfo) API](#9-관광-정보-tourinfo-api)

---

## 1. 인증 (Auth) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/auth/login | 로그인 | Body: LoginRequestDto | CommonResponse<LoginResponseDto> | 공개 |
| POST | /api/auth/signup | 회원가입 | Body: SignupRequestDto | CommonResponse<Void> | 공개 |
| POST | /api/auth/password-reset/request | 비밀번호 재설정 요청 | Body: PasswordResetRequestDto | CommonResponse<Void> | 공개, 이메일 발송 |
| GET | /api/auth/password-reset/verify | 비밀번호 재설정 이메일 인증 | Query: token | CommonResponse<Void> | 공개 |
| POST | /api/auth/password-reset/confirm-email | 이메일 인증 확인 | Body: PasswordResetVerifyRequestDto | CommonResponse<Void> | 공개 |
| POST | /api/auth/password-reset/confirm | 새 비밀번호 설정 | Body: PasswordChangeRequestDto | CommonResponse<Void> | 공개 |
| POST | /api/auth/refresh | 토큰 재발급 | Body: {"refreshToken": "string"} | CommonResponse<LoginResponseDto> | 공개 |
| GET | /api/auth/user | 현재 사용자 정보 조회 | Header: Authorization | CommonResponse<Map<String, Object>> | Auth 필요 |
| POST | /api/auth/check-nickname | 닉네임 중복 확인 | Body: NicknameCheckRequestDto | CommonResponse<NicknameCheckResponseDto> | 공개 |

### 요청/응답 DTO 상세

**LoginRequestDto**
```json
{
  "email": "user@example.com",        // String: 사용자 이메일
  "password": "password123"           // String: 비밀번호
}
```

**LoginResponseDto**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",     // String: 액세스 토큰
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."     // String: 리프레시 토큰
}
```

**SignupRequestDto**
```json
{
  "email": "user@example.com",        // String: 사용자 이메일
  "password": "password123",          // String: 비밀번호
  "nickname": "골프러버"              // String: 닉네임
}
```

**PasswordResetRequestDto**
```json
{
  "email": "user@example.com"         // String: 비밀번호 재설정할 이메일
}
```

**PasswordResetVerifyRequestDto**
```json
{
  "email": "user@example.com"         // String: 인증 확인할 이메일
}
```

**PasswordChangeRequestDto**
```json
{
  "email": "user@example.com",        // String: 사용자 이메일
  "newPassword": "newPassword123"     // String: 새 비밀번호
}
```

**NicknameCheckRequestDto**
```json
{
  "nickname": "골프러버"              // String: 중복 확인할 닉네임
}
```

**NicknameCheckResponseDto**
```json
{
  "isAvailable": true,                // Boolean: 사용 가능 여부
  "message": "사용 가능한 닉네임입니다." // String: 확인 메시지
}
```

**사용자 정보 조회 응답 (GET /api/auth/user)**
```json
{
  "id": 1,                           // Long: 사용자 ID
  "email": "user@example.com",       // String: 이메일
  "nickname": "골프러버",             // String: 닉네임
  "loginType": "EMAIL",              // String: 로그인 타입
  "role": "ROLE_USER"                // String: 사용자 역할
}
```

---

## 2. 커뮤니티 (Community) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/posts | 게시글 작성 | Header: Authorization; Body: CommunityRequestDto + images (multipart) | CommonResponse<CommunityResponseDto> | Auth 필요, 이미지 업로드 가능 |
| GET | /api/posts | 전체 게시글 조회 | - | CommonResponse<List<CommunityResponseDto>> | 공개 |
| GET | /api/posts/category | 카테고리별 게시글 조회 | Query: category | CommonResponse<List<CommunityResponseDto>> | 공개 |
| GET | /api/posts/search | 게시글 검색 | Query: keyword | CommonResponse<List<CommunityResponseDto>> | 공개 |
| GET | /api/posts/{id} | 게시글 단건 조회 | Path: id | CommonResponse<CommunityResponseDto> | 공개 |
| PUT | /api/posts/{id} | 게시글 수정 | Header: Authorization; Path: id; Body: PostUpdateRequestDto + images (multipart) | CommonResponse<CommunityResponseDto> | Auth 필요, 작성자만 |
| DELETE | /api/posts/{id} | 게시글 삭제 | Header: Authorization; Path: id | CommonResponse<Void> | Auth 필요, 작성자만 |
| POST | /api/posts/{id}/like | 게시글 좋아요 토글 | Header: Authorization; Path: id | CommonResponse<Boolean> | Auth 필요 |
| GET | /api/posts/popular | 인기 게시글 TOP3 | - | CommonResponse<List<CommunityResponseDto>> | 공개 |
| GET | /api/posts/popular/category | 카테고리별 인기 TOP3 | Query: category | CommonResponse<List<CommunityResponseDto>> | 공개 |
| GET | /api/posts/likeCount/{id} | 게시글 좋아요 수 | Path: id | CommonResponse<Integer> | 공개 |

### 요청/응답 DTO 상세

**CommunityRequestDto**
```json
{
  "title": "골프장 후기 공유합니다",    // String: 게시글 제목
  "content": "오늘 다녀온 골프장 정말 좋았어요. 코스도 잘 관리되어 있고...", // String: 게시글 내용
  "category": "REVIEW"               // String: 카테고리 (REVIEW, TIP, QNA, FREE 등)
}
```

**PostUpdateRequestDto**
```json
{
  "title": "수정된 제목",             // String: 수정할 제목
  "content": "수정된 내용",           // String: 수정할 내용
  "category": "REVIEW",              // String: 수정할 카테고리
  "imagesToDelete": [1, 2, 3]        // List<Long>: 삭제할 이미지 ID 목록
}
```

**CommunityResponseDto**
```json
{
  "id": 1,                          // Long: 게시글 ID
  "authorId": 10,                   // Long: 작성자 ID
  "title": "골프장 후기 공유합니다",   // String: 게시글 제목
  "content": "오늘 다녀온 골프장...", // String: 게시글 내용
  "author": "골프러버",              // String: 작성자 닉네임
  "category": "REVIEW",             // CommunityCategory: 카테고리 ENUM
  "images": [                       // List<PostImageResponseDto>: 첨부 이미지들
    {
      "id": 1,                      // Long: 이미지 ID
      "url": "https://example.com/image1.jpg", // String: 이미지 URL
      "originalFilename": "golf1.jpg", // String: 원본 파일명
      "size": 1024000               // Long: 파일 크기 (bytes)
    }
  ],
  "createdAt": "2024-01-01T10:00:00", // LocalDateTime: 작성일시
  "updatedAt": "2024-01-01T10:00:00"  // LocalDateTime: 수정일시
}
```

---

## 3. 댓글 (Comment) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/comments | 댓글 작성 | Header: Authorization; Body: CommentRequestDto | CommonResponse<CommentResponseDto> | Auth 필요 |
| GET | /api/comments/my | 내가 작성한 댓글 조회 | Header: Authorization | CommonResponse<List<CommentResponseDto>> | Auth 필요 |
| GET | /api/comments/{commentId} | 댓글 단건 조회 | Path: commentId | CommonResponse<CommentResponseDto> | 공개 |
| PUT | /api/comments/{commentId} | 댓글 수정 | Header: Authorization; Path: commentId; Body: CommentRequestDto | CommonResponse<CommentResponseDto> | Auth 필요, 작성자만 |
| DELETE | /api/comments/{commentId} | 댓글 삭제 | Header: Authorization; Path: commentId | CommonResponse<Void> | Auth 필요, 작성자만 |
| GET | /api/comments/{commentId}/replies | 대댓글 조회 | Path: commentId | CommonResponse<List<CommentResponseDto>> | 공개 |
| GET | /api/comments/post/{postId} | 게시글의 댓글 목록 | Path: postId | CommonResponse<List<CommentResponseDto>> | 공개 |

### 요청/응답 DTO 상세

**CommentRequestDto**
```json
{
  "content": "좋은 글이네요! 저도 그 골프장 가보고 싶어요.", // String: 댓글 내용
  "communityId": 1,                  // Long: 댓글이 달린 커뮤니티 게시글 ID
  "parentCommentId": null            // Long: 대댓글의 경우 부모 댓글 ID, null이면 일반 댓글
}
```

**CommentResponseDto**
```json
{
  "id": 1,                          // Long: 댓글 ID
  "content": "좋은 글이네요!",        // String: 댓글 내용
  "author": "골프매니아",            // String: 작성자 닉네임
  "authorId": 5,                    // Long: 작성자 ID
  "communityId": 1,                 // Long: 게시글 ID
  "parentCommentId": null,          // Long: 부모 댓글 ID (대댓글인 경우)
  "createdAt": "2024-01-01T11:00:00", // LocalDateTime: 작성일시
  "updatedAt": "2024-01-01T11:00:00"  // LocalDateTime: 수정일시
}
```

---

## 4. 골프장 (GolfCourse) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| GET | /api/golf-courses | 전체 골프장 조회 | - | CommonResponse<List<GolfCourseResponseDto>> | 공개 |
| GET | /api/golf-courses/{id} | 골프장 단건 조회 | Path: id | CommonResponse<GolfCourseResponseDto> | 공개 |
| GET | /api/golf-courses/search | 골프장명 검색 | Query: name | CommonResponse<List<GolfCourseResponseDto>> | 공개 |

### 요청/응답 DTO 상세

**GolfCourseResponseDto**
```json
{
  "id": 1,                          // Long: 골프장 ID
  "name": "제주 골프 클럽",           // String: 골프장 이름
  "address": "제주특별자치도 제주시...", // String: 골프장 주소
  "phone": "064-123-4567",          // String: 전화번호
  "website": "https://jeju-golf.com", // String: 웹사이트
  "description": "제주도 최고의 골프장...", // String: 골프장 설명
  "greenFee": 150000,               // Integer: 그린피 (원)
  "holes": 18,                      // Integer: 홀 수
  "par": 72,                        // Integer: 파
  "rating": 4.5,                    // Double: 평점
  "facilities": ["카트", "캐디", "클럽하우스"], // List<String>: 시설 정보
  "mapx": "126.5219",               // String: 경도
  "mapy": "33.4996",                // String: 위도
  "imageUrl": "https://example.com/golf1.jpg" // String: 대표 이미지
}
```

---

## 5. 코스 추천 (CourseRecommendation) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/courses/recommendation | 코스 추천 생성 | Header: Authorization; Query: golfCourseId, teeOffTime, courseType | CommonResponse<CourseRecommendationResponseDto> | Auth 필요 |
| GET | /api/courses/recommendation/my | 내 추천 코스 목록 | Header: Authorization | CommonResponse<List<CourseRecommendationResponseDto>> | Auth 필요 |
| GET | /api/courses/recommendation/{id} | 추천 코스 단건 조회 | Path: id | CommonResponse<CourseRecommendationResponseDto> | 공개 |
| PUT | /api/courses/recommendation/{id} | 추천 코스 수정 | Header: Authorization; Path: id; Query: golfCourseId, teeOffTime, courseType | CommonResponse<CourseRecommendationResponseDto> | Auth 필요, 작성자만 |
| POST | /api/courses/recommendation/multi-day | 다일차 코스 추천 생성 | Header: Authorization; Body: CourseRecommendationRequestDto | CommonResponse<List<CourseRecommendationResponseDto>> | Auth 필요 |

### 요청/응답 DTO 상세

**CourseRecommendationRequestDto**
```json
{
  "golfCourseIds": [1, 2],           // List<Long>: 골프장 ID 목록 (다일차)
  "startDate": "2024-01-01",         // LocalDate: 여행 시작 날짜
  "travelDays": 2,                   // Integer: 여행 기간 (일)
  "teeOffTimes": ["09:00", "09:30"]  // List<String>: 각 일차별 티오프 시간
}
```

**CourseRecommendationResponseDto**
```json
{
  "id": 1,                          // Long: 추천 코스 ID
  "courseTypeLabel": "럭셔리 코스",   // String: 코스 타입 라벨
  "golfCourseName": "제주 골프 클럽", // String: 골프장 이름
  "estimatedEndTime": "17:00",      // String: 예상 종료 시간
  "recommendationOrder": ["food", "tour", "stay"], // List<String>: 추천 순서
  "recommendedPlaces": [            // List<RecommendedPlaceDto>: 추천 장소들
    {
      "type": "restaurant",         // String: 장소 타입
      "name": "제주 흑돼지 맛집",     // String: 장소 이름
      "address": "제주시 ○○로 123", // String: 주소
      "imageUrl": "https://example.com/restaurant.jpg", // String: 이미지 URL
      "distanceKm": 2.5,           // Double: 골프장에서의 거리 (km)
      "mapx": "126.5219",          // String: 경도
      "mapy": "33.4996"            // String: 위도
    }
  ],
  "startDate": "2024-01-01",        // LocalDate: 여행 시작 날짜
  "travelDays": 2,                  // Integer: 여행 기간
  "dayNumber": 1,                   // Integer: 몇일차인지
  "teeOffTime": "09:00"             // String: 티오프 시간
}
```

---

## 6. 저장된 코스 (SavedCourse) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/courses/saved | 코스 저장 | Header: Authorization; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요 |
| **POST** | **/api/courses/saved/from-recommendation** | **추천 코스 ID로 코스 저장** | **Header: Authorization; Body: SaveCourseFromRecommendationRequestDto** | **CommonResponse<SavedCourseResponseDto>** | **Auth 필요, 스케줄 자동 반영** |
| GET | /api/courses/saved/my | 내 저장된 코스 목록 | Header: Authorization | CommonResponse<List<SavedCourseResponseDto>> | Auth 필요 |
| GET | /api/courses/saved/public | 공개된 코스 목록 | - | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/public/type/{courseType} | 코스 타입별 공개 코스 조회 | Path: courseType | CommonResponse<List<SavedCourseResponseDto>> | 공개 |
| GET | /api/courses/saved/{courseId} | 저장된 코스 단건 조회 | Path: courseId | CommonResponse<SavedCourseResponseDto> | 공개 |
| PUT | /api/courses/saved/{courseId} | 저장된 코스 수정 | Header: Authorization; Path: courseId; Body: SavedCourseRequestDto | CommonResponse<SavedCourseResponseDto> | Auth 필요, 작성자만 |
| **PUT** | **/api/courses/saved/{courseId}/from-recommendation** | **추천 코스 기반 코스 수정/생성 (Upsert)** | **Header: Authorization; Path: courseId; Body: SaveCourseFromRecommendationRequestDto** | **CommonResponse<SavedCourseResponseDto>** | **Auth 필요, 코스 없으면 생성, 스케줄 자동 반영** |
| DELETE | /api/courses/saved/{courseId} | 저장된 코스 삭제 | Header: Authorization; Path: courseId | CommonResponse<Void> | Auth 필요, 작성자만 |

### 요청/응답 DTO 상세

**SavedCourseRequestDto**
```json
{
  "courseName": "제주도 2박3일 골프 여행",  // String: 코스 이름
  "description": "제주도 최고 골프장과 맛집 투어", // String: 코스 설명
  "courseType": "luxury",             // String: 코스 타입 ("luxury", "value", "resort", "theme")
  "startDate": "2024-01-01",          // LocalDate: 여행 시작 날짜
  "travelDays": 2,                    // Integer: 여행 기간 (일)
  "isPublic": true,                   // Boolean: 공개 여부
  "courseDays": [                     // List<SavedCourseDayDto>: 각 일차별 정보
    {
      "dayNumber": 1,                 // Integer: 몇일차
      "courseDate": "2024-01-01",     // LocalDate: 해당 일차 날짜
      "teeOffTime": "09:00",          // LocalTime: 티오프 시간
      "golfCourseId": 1,              // Long: 골프장 ID
      "places": [                     // List<SavedCoursePlaceDto>: 방문할 장소들
        {
          "type": "restaurant",       // String: 장소 타입 ("food", "tour", "stay")
          "name": "제주 흑돼지 맛집",   // String: 장소 이름
          "address": "제주시 ○○로 123", // String: 주소
          "imageUrl": "https://example.com/restaurant.jpg", // String: 이미지 URL
          "distanceKm": 2.5,          // Double: 거리 (km)
          "mapx": "126.5219",         // String: 경도
          "mapy": "33.4996",          // String: 위도
          "visitOrder": 1             // Integer: 방문 순서
        }
      ]
    }
  ]
}
```

**SaveCourseFromRecommendationRequestDto (새로 추가)**
```json
{
  "courseName": "추천 기반 골프 여행",    // String: 코스 이름
  "description": "추천 코스로 만든 완벽한 여행", // String: 코스 설명
  "isPublic": true,                   // Boolean: 공개 여부
  "recommendationIds": [1, 2, 3]      // List<Long>: 추천 코스 ID 목록
}
```

**SavedCourseResponseDto**
```json
{
  "id": 1,                           // Long: 저장된 코스 ID
  "courseName": "제주도 골프 여행",    // String: 코스 이름
  "description": "제주도 최고의...",   // String: 코스 설명
  "courseType": "luxury",            // String: 코스 타입
  "startDate": "2024-01-01",         // LocalDate: 시작 날짜
  "travelDays": 2,                   // Integer: 여행 기간
  "isPublic": true,                  // Boolean: 공개 여부
  "authorName": "골프러버",           // String: 작성자 이름
  "authorId": 10,                    // Long: 작성자 ID
  "createdAt": "2024-01-01T10:00:00", // LocalDateTime: 생성일시
  "courseDays": [                    // List<SavedCourseDayResponseDto>: 일차별 정보
    {
      "id": 1,                       // Long: 일차 ID
      "dayNumber": 1,                // Integer: 몇일차
      "courseDate": "2024-01-01",    // LocalDate: 날짜
      "teeOffTime": "09:00",         // LocalTime: 티오프 시간
      "golfCourse": {                // GolfCourseResponseDto: 골프장 정보
        "id": 1,
        "name": "제주 골프 클럽",
        "address": "제주특별자치도..."
      },
      "places": [                    // List<SavedCoursePlaceResponseDto>: 방문 장소들
        {
          "id": 1,                   // Long: 장소 ID
          "type": "restaurant",      // String: 장소 타입
          "name": "제주 흑돼지 맛집", // String: 장소 이름
          "address": "제주시 ○○로 123", // String: 주소
          "imageUrl": "https://example.com/restaurant.jpg",
          "distanceKm": 2.5,
          "mapx": "126.5219",
          "mapy": "33.4996",
          "visitOrder": 1
        }
      ]
    }
  ]
}
```

---

## 7. 스케줄 (Schedule) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/schedules | 일정 추가 | Header: Authorization; Body: ScheduleRequestDto; Query: startDateTime, endDateTime | CommonResponse<ScheduleResponseDto> | Auth 필요 |
| GET | /api/schedules | 전체 일정 조회 | Header: Authorization | CommonResponse<List<ScheduleResponseDto>> | Auth 필요 |
| GET | /api/schedules/{id} | 일정 단건 조회 | Header: Authorization; Path: id | CommonResponse<ScheduleResponseDto> | Auth 필요 |
| PUT | /api/schedules/{id} | 일정 수정 | Header: Authorization; Path: id; Body: ScheduleRequestDto; Query: startDateTime, endDateTime | CommonResponse<ScheduleResponseDto> | Auth 필요 |
| DELETE | /api/schedules/{id} | 일정 삭제 | Header: Authorization; Path: id | CommonResponse<Void> | Auth 필요 |

### 요청/응답 DTO 상세

**ScheduleRequestDto**
```json
{
  "title": "골프장 방문",            // String: 일정 제목
  "allDay": false,                  // Boolean: 종일 일정 여부
  "color": "BLUE",                  // ScheduleColor: 일정 색상 (ENUM)
  "category": "골프",               // String: 일정 카테고리
  "location": "제주 골프 클럽"       // String: 장소
}
```

**ScheduleResponseDto**
```json
{
  "id": 1,                          // Long: 스케줄 ID
  "title": "골프장 방문",            // String: 일정 제목
  "startDateTime": "2024-01-01T09:00:00", // String: 시작 일시
  "endDateTime": "2024-01-01T17:00:00",   // String: 종료 일시
  "allDay": false,                  // Boolean: 종일 일정 여부
  "color": "BLUE",                  // ScheduleColor: 일정 색상
  "category": "골프",               // String: 일정 카테고리
  "location": "제주 골프 클럽",      // String: 장소
  "userId": 10                      // Long: 사용자 ID
}
```

---

## 8. 프로필 이미지 (ProfileImage) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| POST | /api/profile/image/upload | 프로필 이미지 업로드 | Header: Authorization; Body: MultipartFile (image) | CommonResponse<ProfileImageResponseDto> | Auth 필요 |
| GET | /api/profile/image | 프로필 이미지 조회 | Header: Authorization | CommonResponse<ProfileImageResponseDto> | Auth 필요 |
| DELETE | /api/profile/image | 프로필 이미지 삭제 | Header: Authorization | CommonResponse<Void> | Auth 필요 |

### 요청/응답 DTO 상세

**ProfileImageResponseDto**
```json
{
  "id": 1,                          // Long: 이미지 ID
  "imageUrl": "https://example.com/profile1.jpg", // String: 이미지 URL
  "originalFilename": "profile.jpg", // String: 원본 파일명
  "size": 512000,                   // Long: 파일 크기 (bytes)
  "userId": 10,                     // Long: 사용자 ID
  "uploadedAt": "2024-01-01T10:00:00" // LocalDateTime: 업로드 일시
}
```

---

## 9. 관광 정보 (TourInfo) API

| Method | Endpoint | 설명 | 요청 | 응답 | 비고 |
|--------|----------|------|------|------|------|
| GET | /api/tour-info/{contentTypeId} | 관광 정보 조회 | Path: contentTypeId; Query: mapX, mapY, radius | CommonResponse<List<TourInfoResponseDto>> | 공개 |

### 요청/응답 DTO 상세

**TourInfoResponseDto**
```json
{
  "contentId": "12345",             // String: 콘텐츠 ID
  "title": "제주 한라산",            // String: 관광지 이름
  "addr1": "제주특별자치도 제주시...", // String: 주소
  "firstImage": "https://example.com/hanla.jpg", // String: 대표 이미지
  "mapx": "126.5219",               // String: 경도
  "mapy": "33.4996",                // String: 위도
  "mlevel": "6",                    // String: 지도 레벨
  "tel": "064-710-7850",            // String: 전화번호
  "dist": "1.5"                     // String: 거리 (km)
}
```

---

## 📝 공통 응답 형식

**CommonResponse<T>**
```json
{
  "statusCode": 200,                // Integer: HTTP 상태 코드
  "msg": "성공 메시지",              // String: 응답 메시지
  "data": "T 타입의 실제 데이터"      // T: 제네릭 타입의 응답 데이터
}
```

## 🔒 인증 방식

- **Header**: `Authorization: Bearer {accessToken}`
- **Cookie**: `accessToken={token}`

## 🎯 특별 기능

### 🆕 새로 추가된 코스 관리 기능
1. **추천 코스 기반 저장**: 여러 추천 코스 ID를 받아서 하나의 저장된 코스로 변환
2. **Upsert 로직**: 코스가 없으면 생성, 있으면 수정
3. **자동 스케줄 동기화**: 코스 저장/수정 시 개인 스케줄에 자동 반영
   - 골프장 일정: 09:00-17:00로 자동 생성
   - 방문 장소 일정: 18:00부터 방문 순서에 따라 1시간씩 배정
4. **변경사항 자동 반영**: 추천 코스의 내용이 변경되면 저장된 코스와 스케줄에도 반영

### 📸 게시글 이미지 업로드
- 멀티파트 폼 데이터로 이미지 업로드 지원
- 게시글 작성/수정 시 여러 이미지 첨부 가능
- 이미지 개별 삭제 기능 (imagesToDelete 배열 사용)

### 💬 댓글 시스템
- 일반 댓글과 대댓글 지원
- 계층형 댓글 구조 (parentCommentId 사용)

---

**🎉 총 54개 엔드포인트의 완전한 통합 API 명세서가 완성되었습니다!**  
모든 요청/응답 DTO의 실제 필드 구조와 타입이 포함되어 있어, 프론트엔드 개발이나 API 테스트 시 바로 활용할 수 있습니다.

# 🪧 RSUPPORT API - 서현지

### ⭐️ 프로젝트 개요

공지사항을 관리하는 RESTful API 서비스로, 공지사항 등록, 수정, 삭제, 조회 기능을 제공합니다.

---

## 📌 핵심 문제 해결 전략

### 1. 파일 업로드

MockMultipartFile을 이용하여 통합 테스트에서 파일 업로드 검증

실제 파일 저장은 FileService에서 처리(로컬 저장소 사용)




### 2. 조회수 증가 (Redis 활용)

Redis를 이용하여 조회수를 캐싱 후 일정 주기마다 DB에 반영

@Scheduled을 사용하여 비동기적으로 처리하여 성능 최적화




### 3. 검색 최적화

제목/내용 검색 시 @Query + LIKE를 사용하여 제시된 검색 기능에 최적인 쿼리 생성




### 4. SessionInterceptor 구현
   
SessionInterceptor를 이용하여 사용자가 로그인 하지 않아도 Http 요청이 들어올 때 HttpSession에 사용자의 id를 저장하도록 구현

저장된 id를 세션에서 불러와 공지를 등록할 때 사용하도록 구현





### 5. 대량 데이터 페이징 처리

Pageable을 활용한 페이징 API 제공

---

## 🛠 실행 방법


### 1. 필수 환경

- Java 17 이상

- Spring Boot 3.x

- H2 Database

- Redis (로컬 실행 필요)

### 2. 데이터 소스 정보

- H2 Console: http://localhost:8080/h2-console

- JDBC URL: jdbc:h2:mem:rsupport

- 사용자명: rsupport

- 비밀번호: rsupport12!@

### 3. Redis 실행 방법

#### Docker를 사용하는 경우
    docker run --name redis -d -p 6379:6379 redis

#### 로컬에서 실행하는 경우 (Redis 설치 필요)
    redis-server

### 4. 테스트 실행 방법


#### 단위 테스트 실행

    ./gradlew test --tests "com.rsupport.api.NoticeServiceTest"

#### 통합 테스트 실행

    ./gradlew test --tests "com.rsupport.api.NoticeIntegrationTest"


---


## 📒 API 명세

### 1. 공지사항 목록 조회
#### **Request**
```http
GET /api/notices
```
| Parameter    | Type     | Required | Description              |
|-------------|----------|----------|--------------------------|
| `searchType` | String  | No       | 검색 타입 (`title` 또는 `titleAndContent`) |
| `keyword`   | String  | No       | 검색어 (제목 또는 제목+내용) |
| `from` | String  | No       | 검색 시작일 (`YYYY-MM-DDTHH:mm:ss`) |
| `to`   | String  | No       | 검색 종료일 (`YYYY-MM-DDTHH:mm:ss`) |
| `page`      | Integer | No       | 페이지 번호 (기본값: 0)  |
| `size`      | Integer | No       | 한 페이지당 항목 수 (기본값: 10) |
| `sort`      | String  | No       | 정렬 기준 필드 (기본값: `createdAt`) |
| `sortDirection`      | String  | No       | 정렬 방식 (기본값: `DESC`) |


#### **Response** (200 OK)
```json
{
    "content": [
        {
          "id": 1,
          "title": "새로운 공지사항",
          "author": "admin",
          "createdAt": "2025-03-18T12:00:00",
          "viewCount": 10,
          "hasAttachment": true
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "first": true,
    "size": 10,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "numberOfElements": 1,
    "empty": false
}
```

---

### 2. 공지사항 상세 조회
#### **Request**
```http
GET /api/notices/{noticeId}
```
#### **Response** (200 OK)
```json
{
  "id": 1,
  "title": "새로운 공지사항",
  "content": "이것은 공지사항의 내용입니다.",
  "author": "admin",
  "createdAt": "2025-03-18T12:00:00",
  "viewCount": 11,
  "attachmentUrls": [
        "uploads/6fcb5615-b83e-4b61-91dd-de934c7a89df-1.png"
    ]
}
```

---

### 3. 공지사항 등록
#### **Request**
```http
POST /api/notices
Content-Type: multipart/form-data
```
| Parameter    | Type     | Required | Description              |
|-------------|----------|----------|--------------------------|
| `title`     | String   | Yes      | 공지사항 제목           |
| `content`   | String   | Yes      | 공지사항 내용           |
| `startAt`   | String   | Yes      | 공지 시작일 (`YYYY-MM-DDTHH:mm:ss`) |
| `endAt`     | String   | Yes      | 공지 종료일 (`YYYY-MM-DDTHH:mm:ss`) |
| `files`     | File[]   | No       | 첨부파일 리스트         |

#### **Response** (200 OK)
```
  공지사항 저장 성공
```

---

### 4. 공지사항 수정
#### **Request**
```http
PUT /api/notices/{id}
Content-Type: multipart/form-data
```
| Parameter    | Type     | Required | Description |
|-------------|----------|----------|-------------|
| `title`     | String   | No       | 공지사항 제목 (없으면 기존값 유지) |
| `content`   | String   | No       | 공지사항 내용 (없으면 기존값 유지) |
| `startAt`   | String   | No       | 공지 시작일 (`YYYY-MM-DDTHH:mm:ss`) (없으면 기존값 유지) |
| `endAt`     | String   | No       | 공지 종료일 (`YYYY-MM-DDTHH:mm:ss`) (없으면 기존값 유지) |
| `files`     | File[]   | No       | 첨부파일 리스트 (새로운 파일 추가 가능) |

#### **Response** (200 OK)
```
  공지사항 수정 성공
```

---

### 5. 공지사항 삭제
#### **Request**
```http
DELETE /api/notices/{id}
```
#### **Response** (200 OK)
```
  공지사항 삭제 성공
```

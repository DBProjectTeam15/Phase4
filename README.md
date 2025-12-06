# Phase 4

## 프로젝트 개요
음악 스트리밍 플랫폼의 플레이리스트 및 댓글 관리 시스템
- Spring Boot 3.5.8 + JDBC Template
- Oracle Database
- React Frontend

---

## 1. 트랜잭션 및 동시성 제어

### 구현 기능

#### 1.1 플레이리스트에 곡 추가 (동시성 제어)
**위치**: `PlaylistRepository.java:198-220`

- `@Transactional(isolation = Isolation.READ_COMMITTED)` 적용
- **SELECT FOR UPDATE**를 통한 비관적 잠금(Pessimistic Locking)
- 중복 곡 추가 방지 (`DuplicateKeyException`)

**프로세스**:
```
1. 플레이리스트 존재 확인 및 잠금 (SELECT FOR UPDATE)
2. 곡 존재 확인
3. 중복 체크
4. 곡 추가 (INSERT)
```

**API 엔드포인트**: `POST /api/playlists/{playlistId}/songs/{songId}`

**응답 코드**:
- `200 OK`: 곡 추가 성공
- `409 Conflict`: 이미 플레이리스트에 추가된 곡
- `404 Not Found`: 플레이리스트 또는 곡을 찾을 수 없음

#### 1.2 댓글 추가 (트랜잭션)
**위치**: `CommentRepository.java:48-70`

- `@Transactional(isolation = Isolation.READ_COMMITTED)` 적용
- 플레이리스트 존재 확인 → 댓글 삽입 → 생성된 댓글 조회까지 하나의 트랜잭션

**API 엔드포인트**: `POST /api/comments/playlists/{playlistId}`

**요청 본문**:
```json
{
  "content": "댓글 내용"
}
```

**응답 코드**:
- `201 Created`: 댓글 추가 성공
- `404 Not Found`: 플레이리스트를 찾을 수 없음
- `401 Unauthorized`: 로그인 필요

### 예외 처리

**위치**: `GlobalExceptionHandler.java`

중앙 집중식 예외 처리로 적절한 HTTP 상태 코드 반환:
- `DuplicateKeyException` → 409 Conflict
- `EntityNotFoundException` → 404 Not Found
- 기타 예외 → 500 Internal Server Error

### 테스트 결과
모든 API 테스트 통과 ✓
- 정상 추가 시나리오
- 중복 추가 차단
- 존재하지 않는 리소스 처리
- 트랜잭션 롤백 동작

---

## 2. 데이터베이스 인덱스 최적화

### 추가된 인덱스 (총 11개)

#### 필수 인덱스 (7개) - 즉시 성능 개선

| 인덱스명 | 테이블 | 컬럼 | 목적 | 관련 코드 |
|---------|--------|------|------|----------|
| `idx_comments_playlist_date` | COMMENTS | Playlist_id, Commented_at DESC | Playlist별 댓글 조회 최적화 | CommentRepository:38-42 |
| `idx_playlists_user` | PLAYLISTS | User_id | 사용자별 Playlist 조회 | PlaylistRepository:85 |
| `idx_madeby_artist` | MADE_BY | Artist_id, Song_id | Artist로 Song 검색 (역방향) | SongRepository:66-75 |
| `idx_artists_name` | ARTISTS | UPPER(Name) | Artist 이름 검색 (대소문자 무시) | ArtistRepository:33 |
| `idx_consistedof_song` | CONSISTED_OF | Song_id | Song이 포함된 Playlist 조회 (역방향) | PlaylistRepository:77 |
| `idx_edits_playlist` | EDITS | Playlist_id | Playlist 편집자 조회 (역방향) | PlaylistRepository:94,107 |
| `idx_request_manager` | SONG_REQUESTS | Manager_id | 관리자별 Song Request 조회 | SongRequestRepository:80 |

#### 선택 인덱스 (4개) - 검색 기능 최적화

| 인덱스명 | 테이블 | 컬럼 | 목적 |
|---------|--------|------|------|
| `idx_songs_provider` | SONGS | Provider_id | Provider별 Song 조회 (FK 조인) |
| `idx_songs_title_upper` | SONGS | UPPER(Title) | Song 제목 검색 (대소문자 무시) |
| `idx_songs_createat` | SONGS | Create_at | 발매일 범위 검색 |
| `idx_providers_name_upper` | PROVIDERS | UPPER(Provider_name) | Provider 이름 검색 |

### 인덱스 설계 근거

#### 역방향 인덱스가 필요한 이유
복합 PK의 경우 첫 번째 컬럼으로만 효율적인 검색 가능:
- `COMMENTS(User_id, Playlist_id, ...)` → Playlist별 조회 시 비효율
- `CONSISTED_OF(Playlist_id, Song_id)` → Song별 조회 시 비효율
- `EDITS(User_id, Playlist_id)` → Playlist별 조회 시 비효율

#### 함수 기반 인덱스 (Function-Based Index)
대소문자 무시 검색을 위해 UPPER() 함수 적용:
- `UPPER(Name)`, `UPPER(Title)`, `UPPER(Provider_name)`

### 적용 방법
```sql
-- schema.sql 실행 시 자동 생성
sqlplus @sql/schema.sql
```

---

## 3. 주요 파일 구조

```
src/main/java/knu/database/musicbase/
├── controller/
│   ├── PlaylistController.java    # 곡 추가/삭제 API
│   └── CommentController.java     # 댓글 추가 API
├── repository/
│   ├── PlaylistRepository.java    # SELECT FOR UPDATE, 트랜잭션
│   └── CommentRepository.java     # 댓글 트랜잭션
├── exception/
│   ├── EntityNotFoundException.java      # 커스텀 예외
│   └── GlobalExceptionHandler.java       # 전역 예외 처리
└── dto/

sql/
└── schema.sql                     # 스키마 + 인덱스 정의

src/main/frontend/src/pages/
└── PlaylistDetailPage.jsx         # 동시성 테스트 UI
```

---

## 4. 개발 환경

- Java 17
- Spring Boot 3.5.8
- Oracle Database
- React 18
- Gradle

---

## 5. 실행 방법

### Backend
```bash
./gradlew bootRun
```

### Frontend
```bash
cd src/main/frontend
npm install
npm start
```

### Database 초기화
```bash
sqlplus username/password@database @sql/schema.sql
sqlplus username/password@database @sql/data.sql
```
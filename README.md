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

### Backend
- **Java**: 17
- **Spring Boot**: 3.5.8
- **빌드 도구**: Gradle
- **데이터베이스**: Oracle Database (JDBC 11)
- **주요 라이브러리**:
  - Spring Data JDBC
  - Spring Web
  - Lombok

### Frontend
- **React**: 19.2.0
- **빌드 도구**: Vite 7.2.4
- **주요 라이브러리**:
  - React Router DOM 7.10.1
  - Axios 1.13.2
  - Bootstrap 5.3.8
  - React Bootstrap 2.10.10

### 개발 도구
- **IDE**: IntelliJ IDEA 또는 Visual Studio Code 권장
- **Node.js**: 18.x 이상
- **pnpm**: 8.x 이상 (패키지 매니저)

---

## 5. 실행 방법

### 사전 요구사항

1. **Java 17** 이상 설치
2. **Node.js 18.x** 이상 및 **pnpm** 설치
3. **Oracle Database** 설치 및 실행 중 (또는 Docker 사용)
4. **Gradle** (프로젝트 내 Gradle Wrapper 사용 가능)

### Database 설정 및 초기화

#### 방법 1: Docker 사용 (권장)

```bash
# 1. Oracle Database 사용자 생성
docker exec -it <CONTAINER_NAME> sqlplus sys/oracle as SYSDBA

# SQL*Plus에서 실행:
CREATE USER musicbase IDENTIFIED BY musicbase1234;
GRANT CONNECT, RESOURCE TO musicbase;
GRANT UNLIMITED TABLESPACE TO musicbase;
exit;

# 2. SQL 파일을 컨테이너에 복사
docker cp ./sql/schema.sql <CONTAINER_NAME>:/tmp/schema.sql
docker cp ./sql/data.sql <CONTAINER_NAME>:/tmp/data.sql

# 3. 스키마 및 데이터 초기화
docker exec -it <CONTAINER_NAME> sqlplus musicbase/musicbase1234 @/tmp/schema.sql
docker exec -it <CONTAINER_NAME> sqlplus musicbase/musicbase1234 @/tmp/data.sql
```

**중요**: SQL*Plus에서 `COMMIT;` 명령어를 반드시 실행하여 변경사항을 저장하세요.

#### 방법 2: 로컬 Oracle Database 사용

```bash
# 1. Oracle Database 사용자 생성
sqlplus sys/password as SYSDBA

# SQL*Plus에서 실행:
CREATE USER musicbase IDENTIFIED BY musicbase1234;
GRANT CONNECT, RESOURCE, DBA TO musicbase;
exit;

# 2. 스키마 및 데이터 초기화
sqlplus musicbase/musicbase1234@localhost:1521/xe @sql/schema.sql
sqlplus musicbase/musicbase1234@localhost:1521/xe @sql/data.sql
```

### Backend 실행

#### 방법 1: Gradle Wrapper 사용 (권장)
```bash
# 프로젝트 루트 디렉토리에서
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

#### 방법 2: IDE에서 실행
- `src/main/java/knu/database/musicbase/MusicBaseApplication.java` 파일을 실행

**실행 확인**: http://localhost:8080

### Frontend 실행

```bash
# 프론트엔드 디렉토리로 이동
cd src/main/frontend

# 의존성 설치 (최초 1회만)
pnpm install

# 개발 서버 실행
pnpm dev
```

**실행 확인**: http://localhost:5173

---

## 6. 주요 기능

### 사용자 기능
- **회원가입 및 로그인**: 사용자 인증 및 세션 관리
- **플레이리스트 관리**: 생성, 수정, 삭제, 곡 추가/삭제
- **댓글 작성**: 플레이리스트에 댓글 작성 및 조회
- **검색 기능**:
  - 곡 검색 (제목, 아티스트, 장르)
  - 아티스트 검색 (이름)
  - 플레이리스트 검색

### 관리자 기능
- **아티스트 관리**: 등록, 수정, 삭제
- **Provider 관리**: 음원 제공자 관리
- **곡 요청 승인**: 사용자가 요청한 곡 승인/거부

### 동시성 제어 기능
- **플레이리스트에 곡 추가**: SELECT FOR UPDATE를 통한 비관적 잠금
- **중복 곡 방지**: 동일한 곡의 중복 추가 차단
- **트랜잭션 처리**: 댓글 추가 등 원자성 보장

---

## 7. 유의 사항

### Database 연결 설정
- 데이터베이스 연결 정보는 `src/main/resources/application-develop.yaml`에서 수정 가능
- 기본 설정:
  ```yaml
  url: jdbc:oracle:thin:@localhost:1521:xe
  username: musicbase
  password: musicbase1234
  ```
- Docker 사용 시 포트 매핑 확인 필요 (예: `-p 1521:1521`)

### 포트 설정
- **Backend**: 8080 (변경 시 `application.yaml`의 `server.port` 수정)
- **Frontend**: 5173 (변경 시 `vite.config.js`의 `server.port` 수정)
- 포트 충돌 시 다른 포트를 사용 중인 프로세스를 종료하거나 포트 변경 필요

### 프록시 설정
- Vite 개발 서버는 `/api` 경로를 `http://localhost:8080`으로 프록시
- Backend API 포트 변경 시 `vite.config.js`의 `proxy.target` 수정 필요

### Oracle Database 버전
- Oracle Database 11g 이상 권장
- Docker 이미지 사용 시: `gvenzl/oracle-xe` 또는 공식 이미지 권장
- JDBC 드라이버: ojdbc11 사용

### 트랜잭션 격리 수준
- 기본 격리 수준: `READ_COMMITTED`
- 동시성 제어가 필요한 작업은 `SELECT FOR UPDATE` 사용

### 개발 모드
- 현재 active profile: `develop`
- 운영 환경 배포 시 별도의 `application-production.yaml` 생성 권장

### 데이터 초기화 주의
- `sql/schema.sql`: 기존 테이블을 DROP하고 재생성하므로 주의
- 데이터 유실을 방지하려면 백업 후 실행

---

## 8. API 엔드포인트

### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `POST /api/auth/register` - 회원가입

### 플레이리스트
- `GET /api/playlists` - 플레이리스트 목록 조회
- `GET /api/playlists/{id}` - 플레이리스트 상세 조회
- `POST /api/playlists` - 플레이리스트 생성
- `PUT /api/playlists/{id}` - 플레이리스트 수정
- `DELETE /api/playlists/{id}` - 플레이리스트 삭제
- `POST /api/playlists/{playlistId}/songs/{songId}` - 곡 추가 (동시성 제어)
- `DELETE /api/playlists/{playlistId}/songs/{songId}` - 곡 삭제

### 댓글
- `GET /api/comments/playlists/{playlistId}` - 댓글 목록 조회
- `POST /api/comments/playlists/{playlistId}` - 댓글 작성 (트랜잭션)
- `DELETE /api/comments/{id}` - 댓글 삭제

### 검색
- `GET /api/songs/search` - 곡 검색
- `GET /api/artists/search` - 아티스트 검색
- `GET /api/playlists/search` - 플레이리스트 검색

---

## 9. 문제 해결

### Backend 실행 실패
```bash
# 포트 8080이 이미 사용 중인 경우
lsof -i :8080  # 사용 중인 프로세스 확인 (macOS/Linux)
kill -9 <PID>  # 프로세스 종료

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# 또는 application.yaml에서 포트 변경
server:
  port: 8081
```

### Frontend 실행 실패
```bash
# node_modules 재설치
cd src/main/frontend
rm -rf node_modules pnpm-lock.yaml
pnpm install

# 포트 5173이 이미 사용 중인 경우
# vite.config.js의 server.port 변경
```

### Database 연결 실패
1. Oracle Database 실행 상태 확인
   ```bash
   # Docker 사용 시
   docker ps | grep oracle

   # 컨테이너 로그 확인
   docker logs <CONTAINER_NAME>
   ```
2. 방화벽 설정 확인 (1521 포트 개방)
3. 연결 정보 확인 (username, password, url)
4. 사용자 권한 확인
5. Docker 사용 시 포트 매핑 확인

### 의존성 문제
```bash
# Gradle 캐시 정리
./gradlew clean build --refresh-dependencies

# pnpm 캐시 정리
pnpm store prune
```

### SQL 실행 시 변경사항이 반영되지 않는 경우
- SQL*Plus에서 `COMMIT;` 명령어를 반드시 실행
- Auto-commit이 비활성화되어 있을 수 있음
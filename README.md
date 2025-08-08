# Mom4U-Server-Spring Repository

## 🚀 프로젝트 개요
- **Framework**: Spring Boot 3.4.5
- **빌드 도구**: Gradle
- **Database**: MySQL 8.0.33
- **배포 방식**: 미정

---

## 🧩 주요 기술 스택

| 기술              | 설명 |
|-----------------|------|
| Spring Boot     | 백엔드 프레임워크 |
| Spring Data JPA | ORM 기반 DB 접근 |
| MySQL 8.0.33    | 개발 / 운영 데이터베이스 |
| Swagger         | API 문서화 도구 (선택 시) |

---

## 📁 프로젝트 구조 (예시)
```angular2html
src/
└── main/
├── java/com/hanium/mom4u/
│   ├── domain/                  # 핵심 비즈니스 도메인 계층
│   │   ├── common/
│   │   ├── member/              # Member 도메인 (엔티티, 서비스 등)
│   │   │   ├── common/
│   │   │   ├── entity/
│   │   │   ├── service/
│   │   │   ├── controller/
│   │   │   ├── repository/
│   │   │   └── dto/
│   │   ├── drug/
│   │   ├── family/
│   │   ├── news/
│   │   └── inquiry/
│   │
│   ├── external/                # 외부 환경설정 관련
│   │
│   └── global/                  # 전역 공통 처리 영역
│       ├── config/              # Security, Swagger 등
│       ├── exception/               # 예외 처리 (Exception, ErrorCode 등)
│       ├── util/                # 유틸리티 클래스
│       └── response/            # 표준 API 응답 포맷
│
└── resources/
├── application.yml          # 환경 설정
├── application-dev.yml      # 개발 환경 설정
└── application-local.yml     # 로컬 환경 설정

```

---
# 📝 Git Commit Convention 📝

DearBelly Spring Server Git 커밋 메시지 작성 규칙

## ✅ 커밋 메시지 형식 ✅

```angular2html
<type>(<scope>): <subject>
```
> 예시 : feat(member): 회원가입 API 구현 <br>
> 이슈 번호를 커밋/PR 메시지에 포함하면, GitHub에서 자동으로 `(#4)` 형식으로 링크되어 작업 추적이 쉬워집니다.

---

## 🧩 Type 목록 🧩

| Type       | 설명 |
|------------|------|
| `feat`     | 새로운 기능 추가 |
| `fix`      | 버그 수정 |
| `refactor` | 기능 변화 없는 리팩토링 |
| `style`    | 코드 포맷팅, 스타일 변경 (세미콜론, 공백 등) |
| `test`     | 테스트 코드 추가/수정 |
| `docs`     | 문서 작성 또는 수정 |
| `chore`    | 빌드, 설정, CI, 기타 유지관리 |

---

# 🌿 Branch Naming Convention 🌿

## 📌 브랜치 네이밍 컨벤션 📌

```angular2html
<type>/<작업-설명>-<이슈번호>
```
- main: 배포 가능한 안정적인 코드
- develop: 개발 브랜치
- `type`: feat, fix, refactor 등
- `작업-설명`: 소문자-케밥케이스(kebab-case)로 작성
- `이슈번호`: GitHub 이슈 번호 연결용
> 예시: `feat/social-login-4`

---

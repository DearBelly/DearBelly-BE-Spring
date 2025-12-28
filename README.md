# 🤱DearBelly🤱

태아와 산모를 위한 맞춤형 관리 서비스, 디어벨리의 SrpingBoot Server Repository 입니다.

---

## 👨‍💻구성원👨‍💻
| 전유연 | 이효린 |
|:---:|:---:|
| <a href="https://github.com/youyeon11"><img src="https://avatars.githubusercontent.com/u/106169205?v=4" width="120"/></a> | <a href="https://github.com/hyo-lin"><img src="https://avatars.githubusercontent.com/u/106169205?v=4" width="120"/></a> |
| [youyeon11](https://github.com/youyeon11) | [hyo-lin](https://github.com/hyo-lin) |

---

## ⭐ERD⭐

![ERD](https://github.com/user-attachments/assets/3f9a8294-7e3c-4d5b-a56d-6d38dda217da)

---

## ⭐Architecture⭐

![Architecture](https://github.com/user-attachments/assets/13794936-81cd-439d-a995-358f75302b93)

---

## 📁 프로젝트 구조 📁
```angular2html
src/
└── main/
├── java/com/hanium/mom4u/
│   ├── domain/                      # 핵심 비즈니스 도메인 계층
│   │   ├── calendar/                # 일정 및 캘린더 도메인
│   │   ├── common/                  # 도메인 공통 로직 및 베이스 코드
│   │   ├── family/                  # 가족 관계 및 그룹 도메인
│   │   ├── letter/                  # 편지 및 메시지 도메인
│   │   ├── member/                  # 회원(Member) 도메인
│   │   ├── news/                    # 정보 도메인
│   │   ├── question/                # 질문 및 Q&A 도메인
│   │   ├── scan/                    # 스캔 및 분석 도메인
│   │   └── sse/                     # 실시간 알림 (Server-Sent Events)
│   │
│   ├── external/                    # 외부 시스템 연동 및 인프라 계층
│   │   ├── redis/                   # Redis 캐시 및 세션 관리
│   │   └── s3/                      # AWS S3 파일 관리
│   │
│   └── global/                      # 전역 공통 처리 영역
│       ├── config/                  # 전역 설정 (Security, Swagger 등)
│       ├── exception/               # 전역 예외 처리
│       ├── filter/                  # 공통 필터
│       ├── response/                # 표준 API 응답 포맷
│       └── util/                    # 공통 유틸리티
│
└── resources/
├── application.yml              # 공통 애플리케이션 설정
├── application-dev.yml          # 개발 환경 설정
├── application-local.yml        # 로컬 환경 설정
├── application-prod.yml         # 운영 환경 설정
│
├── logback-dev.yml              # 개발 환경 로그 설정
└── logback-spring.xml           # Logback 메인 설정 파일
```

---
# 📝 Git Commit Convention 📝

DearBelly Spring Server Git 커밋 메시지 작성 규칙

## 커밋 메시지 형식

```angular2html
<type>(<scope>): <subject>
```
> 예시 : feat(member): 회원가입 API 구현 <br>
> 이슈 번호를 커밋/PR 메시지에 포함하면, GitHub에서 자동으로 `(#4)` 형식으로 링크되어 작업 추적이 쉬워집니다.

---

## Type 목록

| Type       | 설명 |
|------------|------|
| `feat`     | 새로운 기능 추가 |
| `fix`      | 버그 수정 |
| `refactor` | 기능 변화 없는 리팩토링 |
| `del`      | 불필요한 코드 삭제 |
| `test`     | 테스트 코드 추가/수정 |
| `docs`     | 문서 작성 또는 수정 |
| `chore`    | 빌드, 설정, CI, 기타 유지관리 |

---

# 📝 Branch Naming Convention 📝

## 브랜치 네이밍 컨벤션

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

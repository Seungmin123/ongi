# 🥗 사용자 활동 & 영양소 기반 레시피 추천 서비스

사용자의 **신체 정보**, **활동 데이터**, **영양소 요구량**, **알러지 및 선호 식재료**를 기반으로  
최적의 레시피를 추천하고, 대규모 사용자 데이터를 활용한 **Batch 기반 추천 모델**까지 제공하는 통합 영양 추천 플랫폼입니다.

아키텍처는 **JDBC 기반 Spring MVC 서비스 + Messaging Server + Kafka + Redis + Spark Batch Pipeline**으로 구성되어 있습니다.  
확장성과 고성능을 최우선으로 고려한 Event-driven 구조를 갖추고 있습니다.

---

# 🚀 주요 기능

- 사용자 활동 데이터(일반 조회, 추천 레시피 조회, 좋아요, 저장) 기반 연관 / 선호 레시피 추천 - 체류 시간, 스크롤 등 데이터 추가 예정
- 영양소 기반 레시피 추천
- 식재료 마스터/레시피/유저 정보 제공
- Kafka 기반 서비스 간 비동기 이벤트 통신
- Outbox 패턴 → 이벤트 유실 방지
- Redis 캐시로 고속 조회 지원
- **Apache Spark + Kafka Batch Pipeline**으로 사용자 추천 데이터 생성
- LGTM 기반 Observability 구축

---

# ⚙ 기술 스택 (Tech Stack)

| 영역 | 기술 |
|------|------|
| Language | **Java 25** |
| Backend Framework | **Spring Boot 4.0.1-SNAPSHOT** |
| API 처리 | JDBC 기반 Spring MVC(레시피, 식재료 마스터, 유저 등) |
| ORM | Spring Data JPA |
| Messaging | Kafka |
| Pattern | Outbox, Event-driven Architecture |
| Cache | Redis |
| Front-end | React By Figma Maker |
| Observability | **LGTM Stack (Loki + Grafana + Tempo + Mimir)** |
| DB | MySQL |
| Build | Gradle Multi-module |
| Infra | Docker, Kubernetes |
| Batch / Data Pipeline | **Apache Spark, Kafka** |

---

# 🏛 아키텍처 개요

```text
                       ┌─────────────────────────────┐
                       │          Front-end          │
                       └───────────────▲─────────────┘
                                       │
                       ┌───────────────┼─────────────┐
                       │          API Gateway        │ (Spring MVC)
                       └───────────────▲─────────────┘
                                       │
    ┌──────────────────────────────────┼───────────────────────────────────┐
    │                                  │                                   │
┌───┴─────────┐               ┌────────┴──────────┐                 ┌──────┴─────────┐
│ User Service│               │ Ingredient Service│                 │ Recipe Service │
│   (JDBC)    │               │      (JDBC)       │                 │     (JDBC)     │
└────▲────────┘               └────────▲──────────┘                 └──────▲─────────┘
     │                                 │                                   │
     │                                 │                                   │
┌────┴─────┐                  ┌────────┴──────────┐                 ┌──────┴─────────┐
│ Activity │                  │ Messaging Service │                 │ Recommendation │
│ Service  │                  │  (WebFlux / 예정)  │                 │    Service     │
│  (JDBC)  │                  └────────▲──────────┘                 │     (JDBC)     │
└────▲─────┘                           │                            └──────▲─────────┘
     │                                 │                                   │
     │                           Kafka Event Bus                           │
     └─────────────────────────────────┼───────────────────────────────────┘
                                       │
                                ┌──────┴──────────┐
                                │    Outbox DB    │
                                └─────────────────┘

  ┌─────────────────────────────┐           ┌─────────────────────────────┐
  │           MySQL             │           │            Redis            │
  └─────────────────────────────┘           └─────────────────────────────┘

  ┌──────────────────────────────────────────────────────────────────────┐
  │                      Apache Spark Batch System                       │
  │  - Kafka 스트림 기반 사용자 활동 데이터 수집                                 │
  │  - 대규모 사용자 행동 데이터 분석                                           │
  │  - Spark 기반 추천 Score 계산                                           │
  │  - Batch 결과를 Recommendation 서비스에 반영                              │
  └──────────────────────────────────────────────────────────────────────┘

# MSA 기반 서비스 아키텍처 구축 프로젝트

## 📌 Goal
본 프로젝트는 **MSA(Microservices Architecture)** 기반으로 서비스 아키텍처를 구축하고, 확장성과 안정성을 강화하는 것을 목표로 합니다.  
주요 목표는 다음과 같습니다:
1. **이벤트 기반 아키텍처** → 서비스 간 느슨한 결합으로 확장성과 복원력 강화
2. **Kubernetes 오케스트레이션** → 자동 확장, 무중단 배포, 고가용성 확보
3. **Observability 환경 구축** → 로그·메트릭 통합 분석, 빠른 장애 탐지·해결

---

## 🚨 Problem
과거에는 **전화 주문 중심**이었으나, 이제는 **앱 기반 실시간 주문**이 일반화되었습니다.  
하지만 기존 컨테이너 기반 MSA 환경은 프로덕션 수준에 적합하지 않았습니다.

- 서비스 간 **직접 API 호출** → 독립적 확장 어려움, 장애 전파 위험
- **단순 컨테이너 실행** → 배포·확장·장애 대응 자동화 한계
- **분산 환경** → 호출 추적 및 오류 원인 분석, 성능 병목 파악 어려움

---

## 🏗️ Architecture
- **Event-driven Architecture** 기반 메시지 브로커 활용
- **Kubernetes** 클러스터에서 MSA 서비스 오케스트레이션
- **CI/CD 파이프라인**: GitHub Actions (CI) + ArgoCD (CD): Canary 배포
- **Canary Deployment** 전략을 통해 점진적 트래픽 전환 및 안정성 확보

---

## ⚙️ Tech Stack
- **Backend**: Spring Boot (Java), Redis, Kafka
- **AI**: Python
- **Infra**: AWS EKS, ECR, MSK , Kubernetes, Helm
- **CI/CD**: GitHub Actions, ArgoCD
- **Monitoring**: Prometheus, Grafana, Loki

---

## ✅ Key Features
- **자동 확장 & 무중단 배포** (Kubernetes HPA + Canary Deployment)
- **안정적인 배포 파이프라인** (CI/CD 분리 운영)
- **관측 가능성 확보** (로그·메트릭·트레이싱 통합)
- **AWS Cognito 기반 인증** (JWT 토큰, 그룹 기반 권한 관리)
- **이벤트 기반 아키텍처** (Kafka를 통한 서비스 간 비동기 통신)
- **Toss Payments 연동** (결제 승인/취소 처리)
- **Spring Batch 기반 데이터 처리** (리뷰 데이터 반정규화)
- **AI 채팅 기능** (Redis Streams를 통한 실시간 응답)
- **Saga 패턴 기반 분산 트랜잭션** (주문-결제-재고 일관성 보장)
- **캐시 버전 관리** (장바구니 데이터 일관성)
- **MongoDB 연동** (AI 문서 저장 및 검색)

---

```
goormOne_msa/
├── .github/                 # 워크플로우 및 템플릿
│   ├── ISSUE_TEMPLATE/      # 이슈 템플릿
│   └── workflows/           # 서비스별 CI/CD 파이프라인
├── msa-auth-service/        # 인증 서비스
│   └── authservice/
│       ├── config/          # 설정
│       ├── controller/      # 컨트롤러
│       ├── dto/             # DTO
│       ├── entity/          # 엔티티
│       ├── repository/      # 레포지토리
│       └── service/         # 서비스 로직
├── msa-order-service/       # 주문 서비스
│   └── msaorderservice/
│       ├── cart/            # 장바구니
│       ├── config/          # 설정
│       ├── order/           # 주문 (controller, dto, entity, kafka, service)
│       └── …
├── msa-payment-service/     # 결제 서비스
│   └── msapaymentservice/
│       ├── client/          # Toss/외부 API
│       ├── controller/      # 결제 API
│       ├── dto/             # DTO
│       ├── entity/          # 엔티티
│       ├── kafka/           # Kafka (consumer, producer)
│       └── service/         # 결제 서비스 로직
├── msa-store-service/       # 매장/재고 서비스
│   └── storeservice/
│       ├── aop/             # 분산 락
│       ├── batch/           # 리뷰 배치 처리
│       ├── chat/            # AI 채팅
│       ├── controller/      # 매장 API
│       ├── entity/          # 엔티티
│       ├── mongoDB/         # MongoDB 연동
│       ├── repository/      # 레포지토리
│       ├── service/         # 서비스 로직
│       └── stock/           # 재고 관리 (Saga 패턴)
├── msa-user-service/        # 사용자 서비스
│   └── userservice/
│       ├── controller/      # 사용자 API
│       ├── dto/             # DTO
│       ├── entity/          # 엔티티
│       ├── repository/      # 레포지토리
│       └── service/         # 서비스 로직
├── msa-common/              # 공통 라이브러리
├── msa-common-service/      # 공통 서비스 (보안 필터 등)
├── msa-eureka/              # 서비스 디스커버리 (Eureka Server)
├── msa-gateway/             # API 게이트웨이 (Spring Cloud Gateway)
├── msa-auth-lambda/         # AWS Lambda 인증 서비스
├── goormOne_kubernetes/     # 쿠버네티스 배포 (Helm Charts)
├── docker-compose.kafka.yml # Kafka 개발 환경
├── .env                     # 환경 변수
└── README.md

```
"""


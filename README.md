# # MSA 기반 서비스 아키텍처 구축 프로젝트

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

---

```
 goormOne_msa/
├── .github/                       # GitHub 워크플로우 및 템플릿
│   ├── ISSUE_TEMPLATE/            # 이슈 템플릿
│   │   ├── ✨feature.md
│   │   ├── 🐞bug.md
│   │   ├── 📄docs.md
│   │   └── 🛠️fix.md
│   ├── workflows/                 # CI/CD 파이프라인 (각 서비스별)
│   │   ├── auth-workflow.yml
│   │   ├── common-workflow.yml
│   │   ├── order-workflow.yml
│   │   ├── payment-workflow.yml
│   │   ├── store-workflow.yml
│   │   └── user-workflow.yml
│   └── PULL_REQUEST_TEMPLATE.md   # PR 템플릿
├── .gradle/                       # Gradle 빌드 캐시 및 메타데이터
├── gradle/                        # Gradle Wrapper 파일
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── logs/                          # 애플리케이션 로그 파일
├── msa-auth-lambda/               # AWS Lambda 인증 서비스
│   ├── src/main/java/com/example/
│   │   ├── authorizer/
│   │   │   └── LambdaAuthorizerHandler.java
│   │   └── Main.java
│   └── build.gradle
├── msa-auth-service/              # 인증 서비스 (Spring Boot)
│   ├── src/main/java/com/example/authservice/
│   │   ├── config/                # 설정 클래스
│   │   │   ├── CognitoConfig.java
│   │   │   ├── PasswordConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/            # REST API 컨트롤러
│   │   │   ├── AuthController.java
│   │   │   └── HealthController.java
│   │   ├── dto/                   # 데이터 전송 객체
│   │   │   ├── LoginReq.java
│   │   │   ├── LoginRes.java
│   │   │   ├── RegisterCustomerReq.java
│   │   │   └── RegisterOwnerReq.java
│   │   ├── entity/                # JPA 엔티티
│   │   │   ├── admin/
│   │   │   ├── customer/
│   │   │   └── owner/
│   │   ├── exception/             # 예외 처리
│   │   ├── repository/            # 데이터 접근 계층
│   │   ├── service/               # 비즈니스 로직
│   │   │   ├── AuthService.java
│   │   │   └── CognitoService.java
│   │   └── AuthServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── build.gradle
├── msa-common/                    # 공통 라이브러리 모듈
│   └── src/main/java/com/example/common/
│       ├── config/                # 공통 설정
│       ├── dto/                   # 공통 DTO
│       │   ├── ApiResponse.java
│       │   ├── AuthHeaders.java
│       │   └── RequestUser.java
│       ├── entity/                # 공통 엔티티
│       │   ├── AuditBaseEntity.java
│       │   ├── PaymentStatus.java
│       │   └── Role.java
│       ├── exception/             # 공통 예외 처리
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       └── web/                   # 웹 관련 유틸리티
│           ├── AuthUser.java
│           └── AuthUserArgumentResolver.java
├── msa-common-service/            # 공통 서비스 (보안 필터 등)
│   └── src/main/java/com/example/commonservice/
│       ├── security/              # 보안 관련
│       │   ├── CommonAuthContextFilter.java
│       │   ├── HeaderNames.java
│       │   └── PrincipalType.java
│       └── CommonServiceApplication.java
├── msa-eureka/                    # 서비스 디스커버리 (Eureka Server)
│   └── src/main/java/com/example/eureka/
│       └── MsaEurekaApplication.java
├── msa-gateway/                   # API 게이트웨이 (Spring Cloud Gateway)
│   └── src/main/java/com/example/gateway/
│       ├── filter/                # 게이트웨이 필터
│       │   └── PrincipalHeaderGlobalFilter.java
│       ├── resolver/              # 사용자 정보 해결
│       │   ├── UserResolverClient.java
│       │   └── WebClientConfig.java
│       ├── security/              # 보안 설정
│       │   ├── JwtRoleConverter.java
│       │   └── SecurityConfig.java
│       └── ApiGatewayApplication.java
├── msa-order-service/             # 주문 서비스
│   └── src/main/java/com/example/msaorderservice/
│       ├── cart/                  # 장바구니 관리
│       │   ├── controller/
│       │   │   └── CartController.java
│       │   ├── dto/
│       │   │   ├── CartItemAddReq.java
│       │   │   └── CartItemRes.java
│       │   ├── entity/
│       │   │   ├── CartEntity.java
│       │   │   └── CartItemEntity.java
│       │   ├── repository/
│       │   └── service/
│       │       ├── CartService.java
│       │       └── MenuClient.java
│       ├── config/                # 설정 클래스
│       │   ├── RedisConfig.java
│       │   └── SecurityConfig.java
│       ├── order/                 # 주문 관리
│       │   ├── client/            # 외부 서비스 클라이언트
│       │   │   ├── PaymentClient.java
│       │   │   └── StoreClient.java
│       │   ├── controller/
│       │   │   ├── OrderController.java
│       │   │   └── OrderInternalController.java
│       │   ├── dto/
│       │   │   ├── OrderCreateReq.java
│       │   │   └── OrderCreateRes.java
│       │   ├── entity/
│       │   │   ├── OrderEntity.java
│       │   │   └── OrderItemEntity.java
│       │   ├── kafka/             # Kafka 이벤트 처리
│       │   │   ├── config/
│       │   │   ├── consumer/
│       │   │   └── producer/
│       │   ├── repository/
│       │   └── service/
│       │       └── OrderService.java
│       └── MsaOrderServiceApplication.java
├── msa-payment-service/           # 결제 서비스
│   └── src/main/java/com/example/msapaymentservice/
│       ├── client/                # 외부 서비스 클라이언트
│       │   ├── OrderClient.java
│       │   ├── StoreClient.java
│       │   └── TossPaymentClient.java
│       ├── config/                # 설정 클래스
│       │   ├── RestTemplateConfig.java
│       │   └── SecurityConfig.java
│       ├── controller/
│       │   ├── paymentController.java
│       │   └── paymentInternalController.java
│       ├── dto/                   # Toss Payments 연동 DTO
│       │   ├── PaymentPrepareReq.java
│       │   ├── TossConfirmReq.java
│       │   └── TossPaymentRes.java
│       ├── entity/
│       │   └── PaymentEntity.java
│       ├── kafka/                 # Kafka 이벤트 처리
│       │   ├── consumer/
│       │   └── producer/
│       ├── repository/
│       ├── service/
│       │   └── PaymentService.java
│       └── MsaPaymentServiceApplication.java
├── msa-store-service/             # 매장 서비스
│   └── src/main/java/com/example/storeservice/
│       ├── aop/                   # 분산 락 AOP
│       │   ├── DistributedLock.java
│       │   └── DistributedLockAspect.java
│       ├── batch/                 # 배치 처리 (리뷰 데이터)
│       │   ├── ReviewQueryJobConfiguration.java
│       │   └── ReviewsJobConfiguration.java
│       ├── chat/                  # AI 채팅 기능
│       │   ├── ChatRequestMsg.java
│       │   └── ChatStreamGateway.java
│       ├── config/
│       │   └── RedisConfig.java
│       ├── controller/
│       │   ├── MenuController.java
│       │   ├── MenuInventoryController.java
│       │   ├── ReviewController.java
│       │   └── StoreController.java
│       ├── dto/
│       │   ├── MenuDto.java
│       │   ├── ReviewDto.java
│       │   └── StoreDto.java
│       ├── entity/                # JPA 엔티티
│       │   ├── Menu.java
│       │   ├── MenuInventory.java
│       │   ├── Review.java
│       │   └── Store.java
│       ├── global/                # 글로벌 설정 및 예외처리
│       │   ├── config/
│       │   │   ├── AwsS3Config.java
│       │   │   └── QueryDslConfig.java
│       │   ├── exception/
│       │   └── interceptor/
│       ├── mongoDB/               # MongoDB 연동 (AI 문서)
│       │   ├── AiDocumentEntity.java
│       │   └── ReviewQueryEntity.java
│       ├── repository/            # 데이터 접근 계층
│       │   ├── MenuRepository.java
│       │   ├── ReviewRepository.java
│       │   └── StoreRepository.java
│       ├── service/               # 비즈니스 로직
│       │   ├── MenuService.java
│       │   ├── ReviewService.java
│       │   └── StoreService.java
│       ├── stock/                 # 재고 관리 (Saga 패턴)
│       │   ├── model/             # 이벤트 모델
│       │   │   ├── OrderCreatedEvent.java
│       │   │   └── StockReservationResultEvent.java
│       │   ├── service/
│       │   │   ├── StockRedisService.java
│       │   │   └── StockSagaService.java
│       │   └── StockSagaListener.java
│       └── StoreServiceApplication.java
├── msa-user-service/              # 사용자 서비스
│   └── src/main/java/com/example/userservice/
│       ├── controller/
│       │   ├── InternalUserController.java
│       │   └── UserController.java
│       ├── dto/
│       │   ├── MyProfileRes.java
│       │   └── UpdateCustomerReq.java
│       ├── entity/                # 사용자 엔티티
│       │   ├── Customer.java
│       │   ├── CustomerAddress.java
│       │   └── Owner.java
│       ├── repository/
│       ├── service/
│       │   └── UserService.java
│       └── UserServiceApplication.java
├── .env                           # 환경 변수 설정
├── docker-compose.kafka.yml       # Kafka 개발 환경 설정
├── build.gradle                   # 루트 프로젝트 빌드 설정
├── settings.gradle                # 멀티 모듈 프로젝트 설정
└── README.md                      # 프로젝트 문서

```
"""


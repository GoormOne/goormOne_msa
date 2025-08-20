FROM gradle:8.8-jdk17-jammy AS builder
WORKDIR /build

# --- 먼저 gradle 설정 파일만 복사 ---
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# --- 의존성 다운로드 (빌드 실패는 무시해도 캐시 생성됨) ---
RUN gradle build --no-daemon || return 0

# --- 나머지 소스 복사 ---
COPY . .

# --- 실제 빌드 ---
RUN gradle :msa-store-service:build --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/msa-store-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
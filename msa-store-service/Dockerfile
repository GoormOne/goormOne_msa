# --- 1. 빌드 단계 (Builder Stage) ---
# Gradle과 JDK가 포함된 이미지에서 시작
FROM gradle:8.8-jdk17-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 빌드에 필요한 파일들만 먼저 복사하여 Docker 캐시 활용 극대화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Gradle 빌드 실행
RUN gradle build --no-daemon

# --- 2. 실행 단계 (Final Stage) ---
# 가벼운 JRE 이미지에서 시작
FROM eclipse-temurin:17-jre-jammy


WORKDIR /app

# 빌드 단계에서 생성된 Jar 파일만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
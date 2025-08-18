# --- 1. 빌드 단계 (Builder Stage) ---
# Gradle과 JDK가 포함된 이미지에서 시작
FROM gradle:8.8-jdk17-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 프로젝트 전체 파일을 빌드 컨텍스트로 복사
COPY . .

# 2. 특정 서비스 모듈만 빌드하여 Jar 파일 생성
# --no-daemon 옵션은 CI 환경에서 권장됩니다.
RUN gradle :msa-store-service:build --no-daemon


# --- 3. 실행 단계 (Final Stage) ---
# 가벼운 JRE 이미지에서 시작
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 특정 서비스의 Jar 파일만 복사
COPY --from=builder /build/msa-store-service/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
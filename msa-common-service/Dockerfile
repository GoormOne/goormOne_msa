
FROM gradle:8.8-jdk17-jammy AS builder

WORKDIR /build

COPY . .


RUN gradle :msa-common-service:build --no-daemon



FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /build/msa-common-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar","-Dspring.profiles.active=dev", "app.jar"]
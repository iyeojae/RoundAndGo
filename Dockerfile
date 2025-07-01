# --------- Step 1: Build Stage ---------
FROM gradle:8.7.0-jdk21-alpine AS builder

WORKDIR /app

# gradle 캐시 활용을 위해 먼저 설정 파일 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./
COPY gradle.properties ./

# 종속성만 먼저 다운
RUN ./gradlew dependencies --no-daemon || true

# 전체 소스 복사
COPY . .

# 빌드 실행
RUN ./gradlew build --no-daemon

# --------- Step 2: Runtime Stage ---------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/RoundAndGo-0.0.1-SNAPSHOT.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

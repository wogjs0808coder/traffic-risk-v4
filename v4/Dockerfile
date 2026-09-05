# 1단계: 빌드 환경 구성
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# 2단계: 실행 환경 구성
FROM eclipse-temurin:17-jdk
WORKDIR /app

# xgboost4j가 필요로 하는 OpenMP 런타임 설치
RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
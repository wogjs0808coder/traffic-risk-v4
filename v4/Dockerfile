# 1단계: Maven 빌드
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app

# xgboost4j 네이티브 라이브러리 의존성
RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/v4-0.0.1-SNAPSHOT.jar app.jar
COPY ml_artifacts ./ml_artifacts

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

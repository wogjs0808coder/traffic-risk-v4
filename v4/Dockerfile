# 1단계: 빌드 환경 구성
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 현재 디렉토리의 모든 파일을 복사
COPY . .

# Maven 래퍼에 실행 권한 부여 후 빌드 진행 (테스트 생략)
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# 2단계: 실행 환경 구성
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 단계에서 만들어진 jar 파일을 app.jar로 복사
COPY --from=builder /app/target/*.jar app.jar

# Render에서 기본적으로 사용하는 8080 포트 노출
EXPOSE 8080

# 애플리케이션 기동 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

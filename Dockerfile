FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="wralonzo@gmail.com"
LABEL description="API Retail Spring Boot"

WORKDIR /app
COPY target/detail-shop-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
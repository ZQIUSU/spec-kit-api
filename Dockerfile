# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# 学习 JVM 期间使用 JDK 镜像，自带 jps/jstat/jcmd/jstack/jmap 等诊断工具。
# 若要追求最小生产镜像，可改回 eclipse-temurin:17-jre-alpine。
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9080
ENTRYPOINT ["java", "-jar", "app.jar"]

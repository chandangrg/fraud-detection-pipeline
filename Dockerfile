FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -ntp package
FROM eclipse-temurin:17-jre
RUN useradd --system --uid 10001 appuser
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER 10001
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 CMD ["bash","-c","exec 3<>/dev/tcp/127.0.0.1/8081"]
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]

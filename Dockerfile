FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN useradd --system --uid 10001 --home /app app
WORKDIR /app
COPY --from=build /src/target/resume-jd-matcher.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

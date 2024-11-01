FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /project
COPY . .
RUN if [ -d "target" ]; then rm -rf target; fi; \
    ./mvnw dependency:go-offline clean compile package; \
    rm -rf target/original-*.jar # Remove original jar otherwise it will be copied to the final image.

# TODO correct the application name
FROM eclipse-temurin:21-jre
COPY --from=build /project/target/*.jar /app/app.jar
WORKDIR /data
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--help"]

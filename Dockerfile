# syntax=docker/dockerfile:1

# ---- Build: compile and package with the project's Maven wrapper --------------------------------
FROM eclipse-temurin:25-jdk-alpine-3.23 AS build
WORKDIR /workspace

COPY . .
# Tests need Docker (Testcontainers) and a browser; they run in the regular build, not in the image.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests package \
 && java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination /workspace/extracted

# ---- Runtime: JRE only, non-root user ----------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine-3.23

# fontconfig + DejaVu: AWT font support needed by JasperReports to lay out and render the PDF
RUN apk add --no-cache fontconfig ttf-dejavu \
 && addgroup -S tensio \
 && adduser -S -G tensio tensio

WORKDIR /app
# One layer per Spring Boot layer, from the least to the most frequently changed
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER tensio
EXPOSE 8080 8443

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.awt.headless=true"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

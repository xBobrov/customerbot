FROM eclipse-temurin:22-jdk-alpine AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

RUN addgroup -S customerbot && adduser -S customerbot -G customerbot

COPY --from=builder /build/build/libs/*.jar app.jar
RUN chown customerbot:customerbot app.jar

USER customerbot:customerbot

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
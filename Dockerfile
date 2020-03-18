FROM gradle:latest AS BUILD
WORKDIR /build
COPY . .
RUN gradle bootJar
RUN cp build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

FROM openjdk:8-jre-alpine
WORKDIR /app
COPY wait-for-it.sh .
COPY --from=BUILD /build/app.jar app.jar


FROM gradle:6.7.1-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src/gulinav-rest
RUN gradle build --no-daemon

FROM openjdk:11.0.5-jre

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/gulinav-rest/build/libs/gulinav-rest-1.0.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]
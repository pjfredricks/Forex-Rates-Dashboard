FROM openjdk:11-jre-slim

ADD target/rates-dashboard-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
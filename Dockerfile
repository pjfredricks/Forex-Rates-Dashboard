FROM openjdk:11-jdk-slim

ADD target/rates-dashboard-0.0.1.jar rates-dashboard-0.0.1.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "rates-dashboard-0.0.1.jar"]
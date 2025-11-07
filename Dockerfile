FROM eclipse-temurin:18-jdk
COPY ./target/devops.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "devops.jar", "db:3306", "30000"]
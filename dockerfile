FROM maven:latest AS BUILD
COPY ./pom.xml pom.xml
COPY ./lombok.config lombok.config
COPY ./checkstyle.xml checkstyle.xml
COPY ./src/main src/main
RUN mvn clean package

FROM openjdk:17-oracle
COPY --from=BUILD ./target/cloud-file-storage-1.0.0.jar ./cloud-file-storage-1.0.0.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "./cloud-file-storage-1.0.0.jar"]






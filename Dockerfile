FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
COPY . .

EXPOSE 8080

# ENTRYPOINT ["java","-jar","app.jar"]
CMD java -jar target/demo-0.0.1-SNAPSHOT.jar
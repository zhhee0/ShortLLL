FROM eclipse-temurin:17-jre

WORKDIR /app
ARG JAR_FILE=aggregation/target/shortlink-aggregation.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""
EXPOSE 8003

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

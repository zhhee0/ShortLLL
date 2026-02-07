FROM eclipse-temurin:17-jre

WORKDIR /app
ARG JAR_FILE=gateway/target/shortlink-gateway.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""
EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

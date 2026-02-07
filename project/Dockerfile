FROM eclipse-temurin:17-jre

WORKDIR /app
ARG JAR_FILE=project/target/shortlink-project-exec.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""
EXPOSE 8001

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

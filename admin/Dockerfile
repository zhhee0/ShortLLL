FROM eclipse-temurin:17-jre

WORKDIR /app
ARG JAR_FILE=admin/target/shortlink-admin-exec.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""
EXPOSE 8002

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

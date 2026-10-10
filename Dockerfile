# The examples app as a container, e.g. for Railway (railway.toml):
#   docker build -t wicket-oat-examples . && docker run -p 8080:8080 wicket-oat-examples
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
RUN ./mvnw -B -q -pl wicket-oat-examples -am package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/wicket-oat-examples/target/wicket-oat-examples-*.jar app.jar
# Overridable through the host's environment variables
ENV JAVA_TOOL_OPTIONS="-Xmx384m -XX:+UseSerialGC"
EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-Dwicket.configuration=deployment", "-jar", "app.jar"]

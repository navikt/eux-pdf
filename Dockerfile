FROM gcr.io/distroless/java21
COPY target/eux-pdf.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

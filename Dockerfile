FROM gcr.io/distroless/java25
COPY target/eux-pdf.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

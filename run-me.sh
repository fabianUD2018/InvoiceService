./gradlew clean build -DSkipTests
docker build -t invoice_service:latest .
docker-compose up

./gradlew clean build
docker build -t invoice_service:latest .
docker-compose up

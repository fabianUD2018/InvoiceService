./gradlew clean build -x test
docker build -t invoice_service:latest .
docker-compose up

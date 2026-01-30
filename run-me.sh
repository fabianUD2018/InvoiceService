./gradlew clean build -x test
docker build -t invoice_service:local .
docker-compose up

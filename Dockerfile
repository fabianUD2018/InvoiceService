FROM azul/zulu-openjdk:17.0.0-17.28.13-jre
LABEL authors="fcher"
COPY build/libs/InvoiceService-0.0.1-SNAPSHOT.jar app.jar

CMD java -jar ./app.jar


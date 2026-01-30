package smoketest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.javacrumbs.jsonunit.spring.WebTestClientJsonMatcher.json;


@Tag( "smoke")
public class InvoiceServiceSmokeTest {

    private final WebTestClient webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();

    @Test
    void invoiceService_shouldCreateAnInvoice() throws IOException {
        webTestClient.post().uri("/api/v1/invoice")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Files.readString(Paths.get("src/test/resources/create-invoice/create-invoice-request.json")))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .consumeWith(json().isEqualTo(Files.readString(Paths.get("src/test/resources/create-invoice/create-invoice-response.json"))));
    }

    @Test
    void invoiceService_shouldGetAnInvoice() throws IOException {
        InvoiceResponse response = createAnInvoice();
        webTestClient.get().uri("/api/v1/invoice/{id}", response.id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(json().isEqualTo(Files.readString(Paths.get("src/test/resources/get-invoice/get-invoice-response.json"))));
    }

    @Test
    void updateLineItems_shouldReturnOnlyUpdatedLineItems() throws IOException {
        InvoiceResponse response = createAnInvoice();
        webTestClient.patch().uri("/api/v1/invoice/{id}/line-items", response.id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Files.readString(Paths.get("src/test/resources/update-line-items/update-line-item-request.json")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(json().isEqualTo(Files.readString(Paths.get("src/test/resources/update-line-items/update-line-item-response.json"))));
    }

    @Test
    void payInvoice_shouldReturnPaymentResultAndId() throws IOException {
        InvoiceResponse response = createAnInvoice();
        webTestClient.post().uri("/api/v1/invoice/{id}/payment", response.id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Files.readString(Paths.get("src/test/resources/pay-invoice/pay-invoice-request.json")))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(json().isEqualTo(Files.readString(Paths.get("src/test/resources/pay-invoice/pay-invoice-response.json"))));
    }

    private InvoiceResponse createAnInvoice() throws IOException {
        return webTestClient.post().uri("/api/v1/invoice")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Files.readString(Paths.get("src/test/resources/create-invoice/create-invoice-request.json")))
                .exchange().expectBody(InvoiceResponse.class).returnResult().getResponseBody();
    }

    private record InvoiceResponse(String id) {
    }
}

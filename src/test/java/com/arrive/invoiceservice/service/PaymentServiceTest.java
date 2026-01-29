package com.arrive.invoiceservice.service;

import com.arrive.invoiceservice.enums.PaymentProviderResult;
import com.arrive.invoiceservice.mappers.InvoiceMapperImpl;
import com.arrive.invoiceservice.model.request.payments.PayInvoiceRequest;
import com.arrive.invoiceservice.model.request.payments.PaymentMethod;
import com.arrive.invoiceservice.model.response.payment.PaymentResponse;
import com.arrive.invoiceservice.repository.PaymentRepository;
import com.arrive.invoiceservice.repository.entity.invoice.InvoiceEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentEntity;
import com.arrive.invoiceservice.repository.entity.payment.PaymentProvider;
import com.arrive.invoiceservice.repository.entity.payment.PaymentStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.arrive.invoiceservice.utils.InvoiceUtils.createLineItemEntity;
import static com.arrive.invoiceservice.utils.InvoiceUtils.createPayRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private PaymentProviderFactory paymentProviderFactory;

    @Mock
    private PaymentServiceProviderInterface paymentServiceProvider;

    @Spy
    private InvoiceMapperImpl invoiceMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<PaymentEntity> paymentEntityCaptor;


    @ParameterizedTest
    @MethodSource("paymentMethodProvider")
    void processPayment_shouldSavePayment_givenPaymentMethodAndResponseFromMockedService(PaymentMethod paymentMethod, PaymentProviderResult paymentProviderResult, PaymentStatus status, PaymentProvider provider) {
        var invoiceId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        PayInvoiceRequest request = createPayRequest(paymentMethod);
        List<PaymentStatus> expectedStatus = new ArrayList<>();
        InvoiceEntity invoice = InvoiceEntity.builder()
                .id(invoiceId)
                .lineItems(List.of(
                        createLineItemEntity("Item 1", new BigDecimal("100.00")),
                        createLineItemEntity("Item 2", new BigDecimal("200.00"))
                ))
                .build();
        when(paymentRepository.save(any()))
                .thenAnswer(arguments -> {
                            PaymentEntity entity = arguments.getArgument(0);
                            expectedStatus.add(entity.getPaymentStatus());
                            entity.setId(paymentId);
                            return entity;
                        }
                ).thenAnswer(arguments -> {
                            PaymentEntity entity = arguments.getArgument(0);
                            expectedStatus.add(entity.getPaymentStatus());
                            return entity;
                        }
                );

        when(invoiceService.getInvoiceEntity(invoiceId)).thenReturn(invoice);
        when(paymentProviderFactory.getPaymentProvider(paymentMethod)).thenReturn(paymentServiceProvider);
        when(paymentServiceProvider.processPayment(any(PaymentEntity.class))).thenReturn(paymentProviderResult);

        PaymentResponse response = paymentService.processPayment(invoiceId, request);

        verify(paymentRepository, times(2)).save(paymentEntityCaptor.capture());
        List<PaymentEntity> savedPayment = paymentEntityCaptor.getAllValues();
        assertThat(expectedStatus).containsExactly(PaymentStatus.INITIATED, status);
        assertThat(savedPayment.get(0))
                .extracting(PaymentEntity::getId, PaymentEntity::getAmount, PaymentEntity::getPaymentStatus, PaymentEntity::getPaymentProvider, PaymentEntity::getInvoice)
                .containsExactly(paymentId, new BigDecimal("300.00"), status, provider, invoice);
        assertThat(response).extracting(PaymentResponse::getPaymentId, PaymentResponse::getStatus)
                .containsExactly(paymentId.toString(), status.name());
    }

    private static Stream<Arguments> paymentMethodProvider() {
        return Stream.of(
                Arguments.of(PaymentMethod.STRIPE, PaymentProviderResult.SUCCESS, PaymentStatus.PAID, PaymentProvider.STRIPE),
                Arguments.of(PaymentMethod.PAYPAL, PaymentProviderResult.PENDING_CONFIRMATION, PaymentStatus.PENDING, PaymentProvider.PAYPAL),
                Arguments.of(PaymentMethod.PAYPAL, PaymentProviderResult.FAILURE, PaymentStatus.FAILED, PaymentProvider.PAYPAL)
        );
    }
}
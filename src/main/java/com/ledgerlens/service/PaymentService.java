package com.ledgerlens.service;

import com.ledgerlens.dto.PaymentRequestDto;
import com.ledgerlens.dto.PaymentResponseDto;
import com.ledgerlens.entity.Order;
import com.ledgerlens.entity.Payment;
import com.ledgerlens.exception.DuplicateResourceException;
import com.ledgerlens.exception.InvalidReferenceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.OrderRepository;
import com.ledgerlens.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MerchantContext merchantContext;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, MerchantContext merchantContext) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto dto) {
        if (paymentRepository.existsByPaymentId(dto.getPaymentId())) {
            throw new DuplicateResourceException("Payment with paymentId " + dto.getPaymentId() + " already exists");
        }

        String merchantId = merchantContext.merchantId();
        Order order = orderRepository.findByOrderIdAndMerchantId(dto.getOrderId(), merchantId)
                .orElseThrow(() -> new InvalidReferenceException("Referenced order " + dto.getOrderId() + " does not exist"));

        Payment payment = Payment.builder()
                .paymentId(dto.getPaymentId())
                .order(order)
                .merchantId(merchantId)
                .method(dto.getMethod())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .errorCode(dto.getErrorCode())
                .errorDescription(dto.getErrorDescription())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByPaymentId(String paymentId) {
        Payment payment = paymentRepository.findByPaymentIdAndMerchantId(paymentId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " was not found"));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponseDto mapToResponse(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder().getOrderId())
                .merchantId(payment.getMerchantId())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .settlementId(payment.getSettlement() != null ? payment.getSettlement().getSettlementId() : null)
                .errorCode(payment.getErrorCode())
                .errorDescription(payment.getErrorDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

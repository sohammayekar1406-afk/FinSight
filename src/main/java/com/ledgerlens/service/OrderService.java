package com.ledgerlens.service;

import com.ledgerlens.dto.OrderRequestDto;
import com.ledgerlens.dto.OrderResponseDto;
import com.ledgerlens.entity.Order;
import com.ledgerlens.exception.DuplicateResourceException;
import com.ledgerlens.exception.ResourceNotFoundException;
import com.ledgerlens.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MerchantContext merchantContext;

    public OrderService(OrderRepository orderRepository, MerchantContext merchantContext) {
        this.orderRepository = orderRepository;
        this.merchantContext = merchantContext;
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto) {
        if (orderRepository.existsByOrderId(dto.getOrderId())) {
            throw new DuplicateResourceException("Order with orderId " + dto.getOrderId() + " already exists");
        }

        Order order = Order.builder()
                .orderId(dto.getOrderId())
                .merchantId(merchantContext.merchantId())
                .customerId(dto.getCustomerId())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(dto.getStatus())
                .build();

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOrderId(String orderId) {
        Order order = orderRepository.findByOrderIdAndMerchantId(orderId, merchantContext.merchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found"));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findByMerchantId(merchantContext.merchantId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponseDto mapToResponse(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .merchantId(order.getMerchantId())
                .customerId(order.getCustomerId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

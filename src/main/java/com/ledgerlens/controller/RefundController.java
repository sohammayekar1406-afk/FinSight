package com.ledgerlens.controller;

import com.ledgerlens.dto.RefundRequestDto;
import com.ledgerlens.dto.RefundResponseDto;
import com.ledgerlens.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<RefundResponseDto> createRefund(@Valid @RequestBody RefundRequestDto dto) {
        RefundResponseDto response = refundService.createRefund(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponseDto> getRefund(@PathVariable String refundId) {
        RefundResponseDto response = refundService.getRefundByRefundId(refundId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RefundResponseDto>> getAllRefunds() {
        List<RefundResponseDto> response = refundService.getAllRefunds();
        return ResponseEntity.ok(response);
    }
}

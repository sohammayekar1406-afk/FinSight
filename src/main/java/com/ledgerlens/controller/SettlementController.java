package com.ledgerlens.controller;

import com.ledgerlens.dto.SettlementRequestDto;
import com.ledgerlens.dto.SettlementResponseDto;
import com.ledgerlens.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponseDto> createSettlement(@Valid @RequestBody SettlementRequestDto dto) {
        SettlementResponseDto response = settlementService.createSettlement(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponseDto> getSettlement(@PathVariable String settlementId) {
        SettlementResponseDto response = settlementService.getSettlementBySettlementId(settlementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponseDto>> getAllSettlements() {
        List<SettlementResponseDto> response = settlementService.getAllSettlements();
        return ResponseEntity.ok(response);
    }
}

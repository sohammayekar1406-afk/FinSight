package com.ledgerlens.controller;

import com.ledgerlens.dto.FeeRequestDto;
import com.ledgerlens.dto.FeeResponseDto;
import com.ledgerlens.service.FeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @PostMapping
    public ResponseEntity<FeeResponseDto> createFee(@Valid @RequestBody FeeRequestDto dto) {
        FeeResponseDto response = feeService.createFee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeeResponseDto> getFee(@PathVariable UUID id) {
        FeeResponseDto response = feeService.getFeeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FeeResponseDto>> getAllFees() {
        List<FeeResponseDto> response = feeService.getAllFees();
        return ResponseEntity.ok(response);
    }
}

package com.ledgerlens.controller;

import com.ledgerlens.dto.FinancialExceptionResponseDto;
import com.ledgerlens.dto.PagedResponseDto;
import com.ledgerlens.service.FinancialExceptionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
public class FinancialExceptionController {

    private final FinancialExceptionService exceptionService;

    public FinancialExceptionController(FinancialExceptionService exceptionService) {
        this.exceptionService = exceptionService;
    }

    @GetMapping
    public ResponseEntity<?> getAllExceptions(
            @RequestParam(name = "paged", required = false, defaultValue = "false") boolean paged,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        if (paged) {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponseDto<FinancialExceptionResponseDto> response = exceptionService.getExceptionsPaged(pageable);
            return ResponseEntity.ok(response);
        }
        List<FinancialExceptionResponseDto> list = exceptionService.getAllExceptions();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDto<FinancialExceptionResponseDto>> getPagedExceptions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDto<FinancialExceptionResponseDto> response = exceptionService.getExceptionsPaged(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{exceptionId}")
    public ResponseEntity<FinancialExceptionResponseDto> getException(@PathVariable String exceptionId) {
        FinancialExceptionResponseDto response = exceptionService.getExceptionByExceptionId(exceptionId);
        return ResponseEntity.ok(response);
    }
}

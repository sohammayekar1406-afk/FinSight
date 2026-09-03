package com.ledgerlens.controller;

import com.ledgerlens.dto.PagedResponseDto;
import com.ledgerlens.entity.AuditLog;
import com.ledgerlens.repository.AuditLogRepository;
import com.ledgerlens.service.MerchantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final MerchantContext merchantContext;

    public AuditLogController(AuditLogRepository auditLogRepository, MerchantContext merchantContext) {
        this.auditLogRepository = auditLogRepository;
        this.merchantContext = merchantContext;
    }

    @GetMapping
    public ResponseEntity<PagedResponseDto<AuditLog>> getAuditLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        String merchantId = merchantContext.merchantId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditPage = auditLogRepository.findByMerchantId(merchantId, pageable);

        PagedResponseDto<AuditLog> response = PagedResponseDto.<AuditLog>builder()
                .content(auditPage.getContent())
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }
}

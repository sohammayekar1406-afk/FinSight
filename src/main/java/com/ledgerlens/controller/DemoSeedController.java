package com.ledgerlens.controller;

import com.ledgerlens.dto.SeedResponseDto;
import com.ledgerlens.service.SeedDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoSeedController {

    private final SeedDataService seedDataService;

    public DemoSeedController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/seed")
    public ResponseEntity<SeedResponseDto> seedDemoData() {
        SeedResponseDto response = seedDataService.seedDemoData();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<java.util.Map<String, String>> resetDemoData() {
        seedDataService.clearDemoData();
        return ResponseEntity.ok(java.util.Map.of(
                "status", "SUCCESS",
                "message", "Demo financial data reset to clean state (0 records)"
        ));
    }
}

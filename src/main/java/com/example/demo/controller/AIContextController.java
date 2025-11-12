package com.example.demo.controller;

import com.example.demo.dto.request.AIHelpRequestDTO;
import com.example.demo.service.AIContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "6. AI Assistant")
@SecurityRequirement(name = "bearerAuth")
public class AIContextController {

    private final AIContextService aiContextService;

    @PostMapping("/help")
    @Operation(summary = "Ask AI for help with platform usage")
    public ResponseEntity<Map<String, Object>> help(@Valid @RequestBody AIHelpRequestDTO request) {
        Map<String, Object> answer = aiContextService.getHelpAnswer(request);
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/context")
    @Operation(summary = "Get static AI context")
    public ResponseEntity<Map<String, Object>> context() {
        Map<String, Object> ctx = aiContextService.getStaticContext();
        return ResponseEntity.ok(ctx);
    }
}
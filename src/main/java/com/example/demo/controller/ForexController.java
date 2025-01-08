package com.example.demo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.request.ForexRequest;
import com.example.demo.service.ExchangeRateService;

@RestController
@RequestMapping("/api/forex")
public class ForexController {

    private final ExchangeRateService exchangeRateService;

    public ForexController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }
    
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchForexRates() {
        try {
        	exchangeRateService.fetchAndStoreLatestForexRate();
            return ResponseEntity.ok(Map.of(
                "error", Map.of("code", "0000", "message", "成功"),
                "message", "資料已更新"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", Map.of("code", "E001", "message", "批次處理失敗")
            ));
        }
    }

    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> getForexHistory(@RequestBody ForexRequest request) {
        // 日期範圍檢查
        LocalDate startDate = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        LocalDate endDate = LocalDate.parse(request.getEndDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        if (startDate.isBefore(oneYearAgo) || endDate.isAfter(yesterday) || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", Map.of("code", "E001", "message", "日期區間不符")
            ));
        }

        // 取得資料
        List<Map<String, String>> forexHistory = exchangeRateService.getForexHistory(startDate, endDate, request.getCurrency());

        // 成功回應
        return ResponseEntity.ok(Map.of(
            "error", Map.of("code", "0000", "message", "成功"),
            "currency", forexHistory
        ));
    }
}

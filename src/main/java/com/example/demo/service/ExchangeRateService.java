package com.example.demo.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.ForexRateDto;
import com.example.demo.entity.ExchangeRateEntity;
import com.example.demo.repository.ExchangeRateRepository;
import com.example.demo.util.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExchangeRateService {

	private RestTemplate restTemplate;
	private ExchangeRateRepository exchangeRateRepository;

	@Value("${exchange.api.url}")
	private String apiUrl;

	public ExchangeRateService(RestTemplate restTemplate, ExchangeRateRepository exchangeRateRepository) {
		this.restTemplate = restTemplate;
		this.exchangeRateRepository = exchangeRateRepository;
	}

	@Scheduled(cron = "0 0 18 * * ?") // 每日 18:00 執行
//	@Scheduled(cron = "0 */3 * * * ?")
	public void fetchAndStoreLatestForexRate() {
	    ResponseEntity<byte[]> response = restTemplate.exchange("https://openapi.taifex.com.tw/v1/DailyForeignExchangeRates", HttpMethod.GET, null, byte[].class);
	    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
	        try {
	            // 解析 JSON 並轉換成 DTO
	            String jsonResponse = new String(response.getBody(), StandardCharsets.UTF_8);
	            ObjectMapper objectMapper = new ObjectMapper();
	            List<ForexRateDto> rateDTOs = objectMapper.readValue(jsonResponse, new TypeReference<List<ForexRateDto>>() {
	            });

	            // 只保留最新日期的資料
	            Optional<ForexRateDto> latestRate = rateDTOs.stream()
	                    .max(Comparator.comparing(ForexRateDto::getDate));

	            if (latestRate.isPresent()) {
	            	ForexRateDto dto = latestRate.get();
	                LocalDateTime dateTime = DateUtil.parseToLocalDateTime(dto.getDate());
	                BigDecimal rate = new BigDecimal(dto.getRate());

	                // 保存到資料庫
	                ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
	                exchangeRate.setCurrencyPair(dto.getCurrencyPair());
	                exchangeRate.setRate(rate);
	                exchangeRate.setTimestamp(dateTime);
	                exchangeRateRepository.save(exchangeRate);
	            } else {
	                System.err.println("No data available for the latest date.");
	            }
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to parse API response", e);
	        }
	    } else {
	        throw new RuntimeException("Failed to fetch data from API");
	    }
	}

	
	public void fetchAndStoreForexRatesAll() {
	    ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, byte[].class);
	    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
	        try {
	            // 將 byte[] 轉為 JSON 字串
	            String jsonResponse = new String(response.getBody(), StandardCharsets.UTF_8);

	            // 使用 ObjectMapper 將 JSON 轉為 DTO 列表
	            ObjectMapper objectMapper = new ObjectMapper();
	            List<ForexRateDto> rateDTOs = objectMapper.readValue(jsonResponse,
	                    new TypeReference<List<ForexRateDto>>() {});

	            List<ExchangeRateEntity> entitiesToSave = new ArrayList<>();

	            rateDTOs.forEach(dto -> {
	                try {
	                    if (dto.getRate() != null && dto.getDate() != null) {
	                        LocalDateTime dateTime = DateUtil.parseToLocalDateTime(dto.getDate());

	                        ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
	                        exchangeRate.setCurrencyPair(dto.getCurrencyPair());
	                        exchangeRate.setRate(new BigDecimal(dto.getRate()));
	                        exchangeRate.setTimestamp(dateTime);
	                        entitiesToSave.add(exchangeRate);
	                    } else {
	                        System.err.println("Incomplete data: " + dto);
	                    }
	                } catch (Exception e) {
	                    System.err.println("Error processing rate: " + dto + " - " + e.getMessage());
	                }
	            });

	            // 一起保存
	            if (!entitiesToSave.isEmpty()) {
	                exchangeRateRepository.saveAll(entitiesToSave);
	            }
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to parse API response", e);
	        }
	    } else {
	        throw new RuntimeException("Failed to fetch data from API");
	    }
	}


	public List<Map<String, String>> getForexHistory(LocalDate startDate, LocalDate endDate, String currency) {

		List<ExchangeRateEntity> rates = exchangeRateRepository.findByCurrencyPairAndTimestampBetween("USD/NTD",
				startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

		return rates.stream().map(
				rate -> Map.of("date", rate.getTimestamp().toLocalDate().toString(), "usd", rate.getRate().toString()))
				.collect(Collectors.toList());
	}

}

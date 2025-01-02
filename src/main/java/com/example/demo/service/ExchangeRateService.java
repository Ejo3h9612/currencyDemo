package com.example.demo.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.ExchangeRateEntity;
import com.example.demo.repository.ExchangeRateRepository;
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
	public void fetchAndStoreForexRates() {
		
		ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, byte[].class);

		if (response!=null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			try {
				// 將 byte[] 轉為 JSON 字串
				String jsonResponse = new String(response.getBody(), StandardCharsets.UTF_8);

				// 使用 ObjectMapper 解析 JSON
				ObjectMapper objectMapper = new ObjectMapper();
				List<Map<String, Object>> rates = objectMapper.readValue(jsonResponse,
						new TypeReference<List<Map<String, Object>>>() {
						});

				// 過濾並保存數據
				rates.stream()
			     .forEach(rate -> {
			         try {
			        	 
			        	 String rateValue = rate.get("USD/NTD") != null ? rate.get("USD/NTD").toString() : null;
			             String dateTimeValue = rate.get("Date") != null ? rate.get("Date").toString() : null;
			             // 使用自定義的 DateTimeFormatter 解析 yyyyMMdd 格式
		                 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		                 LocalDate date = LocalDate.parse(dateTimeValue, formatter); // 解析為 LocalDate
		                 LocalDateTime dateTime = date.atStartOfDay(); // 將時間設為 00:00:00
		                 
		                 if (rateValue != null && dateTime != null) {
			                 ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
			                 exchangeRate.setCurrencyPair("USD/NTD");
			                 exchangeRate.setRate(new BigDecimal(rateValue));
			                 exchangeRate.setTimestamp(dateTime);
			                 exchangeRateRepository.save(exchangeRate);
			             } else {
			                 // 處理不完整數據的情況，例如記錄日誌或跳過
			                 System.err.println("Incomplete data: " + rate);
			             }
			         } catch (Exception e) {
			             // 捕捉並記錄其他可能的例外
			             System.err.println("Error processing rate: " + rate + " - " + e.getMessage());
			         }
			     });

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

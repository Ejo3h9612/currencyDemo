package com.example.demo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 將 yyyyMMdd 格式的日期字串，返回 LocalDateTime，時間為 00:00:00。
     *
     */
    public static LocalDateTime parseToLocalDateTime(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, FORMATTER);
            return date.atStartOfDay();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
        }
    }
}

package com.one.core.domain.service.tenant.util;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class ProductUtils {

    public String generateSku() {
        String prefix = "SKU-";
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uniquePart;
    }

    public String generateBarcode() {
        String base = String.format("%012d", new Random().nextLong() & Long.MAX_VALUE);
        return base + calculateEAN13Checksum(base);
    }

    private int calculateEAN13Checksum(String code) {
        int sum = 0;
        for (int i = 0; i < code.length(); i++) {
            int digit = Character.getNumericValue(code.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = (10 - (sum % 10)) % 10;
        return checksum;
    }
}

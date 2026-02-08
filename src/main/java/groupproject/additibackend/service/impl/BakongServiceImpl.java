package groupproject.additibackend.service.impl;

import groupproject.additibackend.config.BakongProperties;
import groupproject.additibackend.model.Order;
import groupproject.additibackend.service.BakongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("unchecked")
public class BakongServiceImpl implements BakongService {
    
    private static final Logger log = LoggerFactory.getLogger(BakongServiceImpl.class);
    
    private final BakongProperties bakongProperties;
    private final RestTemplate restTemplate;
    
    public BakongServiceImpl(BakongProperties bakongProperties) {
        this.bakongProperties = bakongProperties;
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public String generateKHQR(Order order) {
        return generateKHQR(
            order.getTotalAmount(),
            bakongProperties.getCurrency(),
            "ORDER-" + order.getId()
        );
    }
    
    @Override
    public String generateKHQR(BigDecimal amount, String currency, String transactionId) {
        try {
            String url = bakongProperties.getApiUrl() + "/v1/generate_deeplink_by_qr";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bakongProperties.getToken());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bank_account", bakongProperties.getAccountId());
            requestBody.put("merchant_name", bakongProperties.getMerchantName());
            requestBody.put("merchant_id", bakongProperties.getMerchantId());
            requestBody.put("amount", amount.doubleValue());
            requestBody.put("currency", currency); // "USD" or "KHR"
            requestBody.put("bill_number", transactionId);
            requestBody.put("store_label", bakongProperties.getMerchantName());
            requestBody.put("terminal_label", "POS-01");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    return (String) data.get("qr"); // The KHQR string
                }
            }
            
            throw new RuntimeException("Failed to generate KHQR");
            
        } catch (Exception e) {
            log.error("Error generating KHQR: {}", e.getMessage());
            // Fallback to manual KHQR generation if API fails
            return generateManualKHQR(amount, currency, transactionId);
        }
    }
    
    /**
     * Manual KHQR generation following EMVCo QR Code Specification
     * This is a fallback if the API is not available
     */
    private String generateManualKHQR(BigDecimal amount, String currency, String transactionId) {
        StringBuilder qr = new StringBuilder();
        
        // Payload Format Indicator
        qr.append("000201");
        
        // Point of Initiation Method (12 = dynamic QR)
        qr.append("010212");
        
        // Merchant Account Information (Tag 29 for Bakong)
        String accountId = bakongProperties.getAccountId();
        String merchantAccountInfo = "0006" + accountId.substring(accountId.indexOf("@") + 1).toUpperCase() + 
                                     "01" + String.format("%02d", accountId.length()) + accountId;
        qr.append("29").append(String.format("%02d", merchantAccountInfo.length())).append(merchantAccountInfo);
        
        // Merchant Category Code
        qr.append("52045999");
        
        // Transaction Currency (840 = USD, 116 = KHR)
        String currencyCode = "USD".equals(currency) ? "840" : "116";
        qr.append("5303").append(currencyCode);
        
        // Transaction Amount
        String amountStr = amount.setScale(2).toPlainString();
        qr.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
        
        // Country Code
        qr.append("5802KH");
        
        // Merchant Name
        String merchantName = bakongProperties.getMerchantName();
        qr.append("59").append(String.format("%02d", merchantName.length())).append(merchantName);
        
        // Merchant City
        qr.append("6010Phnom Penh");
        
        // Additional Data Field (Bill Number)
        String billNumber = "01" + String.format("%02d", transactionId.length()) + transactionId;
        qr.append("62").append(String.format("%02d", billNumber.length())).append(billNumber);
        
        // CRC placeholder (will be calculated)
        qr.append("6304");
        
        // Calculate CRC16
        String crc = calculateCRC16(qr.toString());
        qr.append(crc);
        
        return qr.toString();
    }
    
    private String calculateCRC16(String data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
    
    @Override
    public boolean verifyPayment(String transactionId) {
        PaymentStatusResponse status = checkPaymentStatus(transactionId);
        return status != null && status.paid();
    }
    
    @Override
    public PaymentStatusResponse checkPaymentStatus(String md5Hash) {
        try {
            String url = bakongProperties.getApiUrl() + "/v1/check_transaction_by_md5";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bakongProperties.getToken());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("md5", md5Hash);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    return new PaymentStatusResponse(
                        true,
                        (String) data.get("transaction_id"),
                        new BigDecimal(data.get("amount").toString()),
                        (String) data.get("currency"),
                        (String) data.get("from_account_id"),
                        (String) data.get("created_date")
                    );
                }
            }
            
            return new PaymentStatusResponse(false, null, null, null, null, null);
            
        } catch (Exception e) {
            log.error("Error checking payment status: {}", e.getMessage());
            return new PaymentStatusResponse(false, null, null, null, null, null);
        }
    }
    
    /**
     * Generate MD5 hash of KHQR string for payment verification
     */
    public String generateMD5Hash(String khqrString) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(khqrString.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}

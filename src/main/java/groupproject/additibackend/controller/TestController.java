package groupproject.additibackend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import groupproject.additibackend.config.BakongProperties;
import groupproject.additibackend.khqr.BakongKHQR;
import groupproject.additibackend.khqr.KHQRDecodeData;
import groupproject.additibackend.khqr.KHQRResponse;
import groupproject.additibackend.service.BakongService;

@RestController
public class TestController {

    private final BakongService bakongService;
    private final BakongProperties bakongProperties;

    public TestController(BakongService bakongService, BakongProperties bakongProperties) {
        this.bakongService = bakongService;
        this.bakongProperties = bakongProperties;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("This can be seen after login", HttpStatus.OK);
    }

    /**
     * Test endpoint to generate and debug KHQR
     * Usage: GET /api/test/khqr?amount=10.00
     */
    @GetMapping("/api/test/khqr")
    public ResponseEntity<Map<String, Object>> testKhqr(
            @RequestParam(defaultValue = "1.00") BigDecimal amount) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Show configuration
            result.put("config", Map.of(
                "accountId", bakongProperties.getAccountId() != null ? bakongProperties.getAccountId() : "NOT SET",
                "merchantName", bakongProperties.getMerchantName() != null ? bakongProperties.getMerchantName() : "NOT SET",
                "currency", bakongProperties.getCurrency()
            ));
            
            // Generate KHQR
            BakongService.KHQRResult khqrResult = bakongService.generateKHQR(
                amount,
                bakongProperties.getCurrency(),
                "TEST-" + System.currentTimeMillis()
            );
            
            result.put("khqrString", khqrResult.qr());
            result.put("md5Hash", khqrResult.md5Hash());
            
            // Decode the KHQR to show parsed fields
            KHQRResponse<KHQRDecodeData> decoded = BakongKHQR.decode(khqrResult.qr());
            if (decoded.getKHQRStatus().getCode() == 0) {
                KHQRDecodeData data = decoded.getData();
                result.put("decoded", Map.of(
                    "accountId", data.getBakongAccountId() != null ? data.getBakongAccountId() : "null",
                    "merchantName", data.getMerchantName() != null ? data.getMerchantName() : "null",
                    "amount", data.getTransactionAmount() != null ? data.getTransactionAmount() : "null",
                    "currency", data.getTransactionCurrency() != null ? data.getTransactionCurrency() : "null",
                    "crcValid", data.isCrcValid()
                ));
            }
            
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Test endpoint to verify payment status by MD5
     * Usage: GET /api/test/verify?md5=abc123...
     */
    @GetMapping("/api/test/verify")
    public ResponseEntity<Map<String, Object>> testVerify(@RequestParam String md5) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("md5Hash", md5);
            
            BakongService.PaymentStatusResponse status = bakongService.checkPaymentStatus(md5);
            
            result.put("paid", status.paid());
            result.put("transactionId", status.transactionId());
            result.put("amount", status.amount());
            result.put("currency", status.currency());
            result.put("timestamp", status.timestamp());
            
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test Bakong account lookup
     * Usage: GET /api/test/account?id=phon_ramy@bkrt
     */
    @GetMapping("/api/test/account")
    public ResponseEntity<Map<String, Object>> testAccount(
            @RequestParam(defaultValue = "") String id) {
        Map<String, Object> result = new HashMap<>();
        
        String accountId = id.isEmpty() ? bakongProperties.getAccountId() : id;
        result.put("accountId", accountId);
        result.put("apiUrl", bakongProperties.getApiUrl());
        result.put("tokenConfigured", bakongProperties.getToken() != null && !bakongProperties.getToken().isEmpty());
        
        // Generate a sample KHQR string to show format
        try {
            BakongService.KHQRResult khqrResult = bakongService.generateKHQR(
                new BigDecimal("1.00"),
                "USD",
                "TEST"
            );
            result.put("sampleKhqr", khqrResult.qr());
            result.put("khqrLength", khqrResult.qr().length());
            
            // Parse and show the key parts of the KHQR
            String qr = khqrResult.qr();
            
            // Find Account ID in QR (after "0006bakong01")
            int bakongIndex = qr.indexOf("bakong");
            if (bakongIndex > 0) {
                // After "bakong" there's "01XX" where XX is the account length
                int accountLenStart = bakongIndex + 6 + 2; // after "bakong" + "01"
                if (accountLenStart + 2 <= qr.length()) {
                    int accountLen = Integer.parseInt(qr.substring(accountLenStart, accountLenStart + 2));
                    String accountInQr = qr.substring(accountLenStart + 2, accountLenStart + 2 + accountLen);
                    result.put("accountInQR", accountInQr);
                    result.put("accountMatch", accountInQr.equals(accountId));
                }
            }
            
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
}

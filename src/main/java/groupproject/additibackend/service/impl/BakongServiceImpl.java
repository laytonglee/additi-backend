package groupproject.additibackend.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import groupproject.additibackend.config.BakongProperties;
import groupproject.additibackend.khqr.BakongKHQR;
import groupproject.additibackend.khqr.IndividualInfo;
import groupproject.additibackend.khqr.KHQRCurrency;
import groupproject.additibackend.khqr.KHQRData;
import groupproject.additibackend.khqr.KHQRResponse;
import groupproject.additibackend.model.Order;
import groupproject.additibackend.service.BakongService;

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
    public KHQRResult generateKHQR(Order order) {
        return generateKHQR(
            order.getTotalAmount(),
            bakongProperties.getCurrency(),
            "ORDER-" + order.getId()
        );
    }
    
    @Override
    public KHQRResult generateKHQR(BigDecimal amount, String currency, String transactionId) {
        // Try to use Bakong API first, fall back to local generation if API fails
        try {
            KHQRResult result = generateKHQRViaAPI(amount, currency, transactionId);
            log.info("*** KHQR generated via BAKONG API - Payment verification WILL work ***");
            return result;
        } catch (Exception e) {
            log.warn("*** Failed to generate KHQR via API: {} ***", e.getMessage());
            log.warn("*** Falling back to LOCAL generation - Payment verification will NOT work ***");
            return generateKHQRLocally(amount, currency, transactionId);
        }
    }
    
    /**
     * Generate KHQR using Bakong's official API
     */
    private KHQRResult generateKHQRViaAPI(BigDecimal amount, String currency, String transactionId) {
        String accountId = bakongProperties.getAccountId();
        
        log.info("Generating KHQR via Bakong API for account: {}, amount: {} {}", accountId, amount, currency);
        
        // Bakong API endpoint for generating individual KHQR
        // Try the standard endpoint first
        String url = bakongProperties.getApiUrl() + "/v1/individual/generate_khqr";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bakongProperties.getToken());
        
        // Build request according to Bakong API spec
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bakong_account_id", accountId);
        requestBody.put("amount", amount.doubleValue());
        requestBody.put("currency", currency);
        requestBody.put("merchant_name", bakongProperties.getMerchantName());
        requestBody.put("merchant_city", "Phnom Penh");
        requestBody.put("bill_number", transactionId);
        
        log.info("Bakong API request: URL={}, Body={}", url, requestBody);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            log.info("Bakong generate QR response: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Check for error code
                if (body.containsKey("errorCode") && body.get("errorCode") != null) {
                    Object errorCodeObj = body.get("errorCode");
                    int errorCode = (errorCodeObj instanceof Integer) ? (Integer) errorCodeObj : Integer.parseInt(errorCodeObj.toString());
                    if (errorCode != 0) {
                        log.error("Bakong API error: code={}, message={}", errorCode, body.get("errorMessage"));
                        throw new RuntimeException("Bakong API error: " + body.get("errorMessage"));
                    }
                }
                
                if (body.containsKey("data") && body.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    String qr = (String) data.get("qr");
                    String md5 = (String) data.get("md5");
                    log.info("KHQR generated via API successfully. QR length={}, MD5: {}", 
                        qr != null ? qr.length() : 0, md5);
                    return new KHQRResult(qr, md5);
                }
            }
        } catch (Exception e) {
            log.error("Error calling Bakong API: {}", e.getMessage());
            // Don't throw here, let it fall through to throw the generic error
        }
        
        throw new RuntimeException("Failed to generate KHQR via Bakong API - check logs for details");
    }
    
    /**
     * Generate KHQR locally using our implementation (fallback)
     */
    private KHQRResult generateKHQRLocally(BigDecimal amount, String currency, String transactionId) {
        // Use the SDK-style KHQR generation
        IndividualInfo individualInfo = new IndividualInfo();
        String accountId = bakongProperties.getAccountId();
        
        log.info("Generating KHQR locally for account: {}, amount: {} {}", accountId, amount, currency);
        
        if (accountId == null || accountId.isEmpty()) {
            log.error("Bakong account ID is not configured!");
            throw new RuntimeException("Bakong account ID is not configured. Please set BAKONG_ACCOUNT_ID in .env");
        }
        
        individualInfo.setBakongAccountId(accountId);
        
        // Set acquiring bank based on account domain
        // @bkrt = Bakong Retail
        String acquiringBank = "Bakong";
        if (accountId.contains("@")) {
            String domain = accountId.substring(accountId.indexOf("@") + 1);
            switch (domain.toLowerCase()) {
                case "bkrt":
                    acquiringBank = "Bakong Retail";
                    break;
                case "aclb":
                    acquiringBank = "ACLEDA Bank";
                    break;
                case "wing":
                    acquiringBank = "Wing Bank";
                    break;
                default:
                    acquiringBank = domain.toUpperCase();
            }
        }
        individualInfo.setAcquiringBank(acquiringBank);
        
        individualInfo.setCurrency("USD".equals(currency) ? KHQRCurrency.USD : KHQRCurrency.KHR);
        individualInfo.setAmount(amount.doubleValue());
        individualInfo.setMerchantName(bakongProperties.getMerchantName());
        individualInfo.setMerchantCity("Phnom Penh");
        individualInfo.setBillNumber(transactionId);
        individualInfo.setStoreLabel(bakongProperties.getMerchantName());
        individualInfo.setTerminalLabel("POS-01");
        
        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);
        
        if (response.getKHQRStatus().getCode() == 0) {
            log.info("KHQR generated locally. Account: {}, Bank: {}, MD5: {}", accountId, acquiringBank, response.getData().getMd5());
            log.debug("KHQR string: {}", response.getData().getQr());
            return new KHQRResult(response.getData().getQr(), response.getData().getMd5());
        } else {
            log.error("Failed to generate KHQR: {}", response.getKHQRStatus().getMessage());
            throw new RuntimeException("Failed to generate KHQR: " + response.getKHQRStatus().getMessage());
        }
    }
    
    /**
     * Generate KHQR using IndividualInfo (SDK-style)
     */
    public KHQRResponse<KHQRData> generateIndividualKHQR(IndividualInfo info) {
        return BakongKHQR.generateIndividual(info);
    }
    
    @Override
    public boolean verifyPayment(String transactionId) {
        PaymentStatusResponse status = checkPaymentStatus(transactionId);
        return status != null && status.paid();
    }
    
    @Override
    public PaymentStatusResponse checkPaymentStatus(String md5Hash) {
        try {
            log.info("Checking payment status for MD5: {}", md5Hash);
            
            String url = bakongProperties.getApiUrl() + "/v1/check_transaction_by_md5";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bakongProperties.getToken());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("md5", md5Hash);
            
            log.info("Calling Bakong API: URL={}, Body={}", url, requestBody);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            log.info("Bakong API response: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Check responseCode (0 = success, 1 = error/not found)
                Integer responseCode = body.get("responseCode") != null ? 
                    ((Number) body.get("responseCode")).intValue() : null;
                String responseMessage = (String) body.get("responseMessage");
                
                log.info("Bakong responseCode={}, responseMessage={}", responseCode, responseMessage);
                
                // responseCode 0 means success
                if (responseCode != null && responseCode == 0 && body.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    log.info("Payment found! Transaction: {}, Amount: {} {}, From: {}", 
                        data.get("hash"), data.get("amount"), data.get("currency"), data.get("fromAccountId"));
                    
                    return new PaymentStatusResponse(
                        true,
                        (String) data.get("hash"),
                        data.get("amount") != null ? new BigDecimal(data.get("amount").toString()) : null,
                        (String) data.get("currency"),
                        (String) data.get("fromAccountId"),
                        null // Bakong doesn't return time in this response
                    );
                } else {
                    // responseCode 1 means not found or failed
                    log.info("Payment not found: responseCode={}, message={}", responseCode, responseMessage);
                    return new PaymentStatusResponse(false, null, null, null, null, null);
                }
            }
            
            log.info("Payment not found yet for MD5: {}", md5Hash);
            return new PaymentStatusResponse(false, null, null, null, null, null);
            
        } catch (Exception e) {
            log.error("Error checking payment status for MD5 {}: {}", md5Hash, e.getMessage(), e);
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

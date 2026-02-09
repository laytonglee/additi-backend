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
        // Use the SDK-style KHQR generation
        IndividualInfo individualInfo = new IndividualInfo();
        individualInfo.setBakongAccountId(bakongProperties.getAccountId());
        individualInfo.setAcquiringBank(bakongProperties.getMerchantName());
        individualInfo.setCurrency("USD".equals(currency) ? KHQRCurrency.USD : KHQRCurrency.KHR);
        individualInfo.setAmount(amount.doubleValue());
        individualInfo.setMerchantName(bakongProperties.getMerchantName());
        individualInfo.setMerchantCity("PHNOM PENH");
        individualInfo.setBillNumber(transactionId);
        individualInfo.setStoreLabel(bakongProperties.getMerchantName());
        individualInfo.setTerminalLabel("POS-01");
        
        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);
        
        if (response.getKHQRStatus().getCode() == 0) {
            log.info("KHQR generated successfully. MD5: {}", response.getData().getMd5());
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

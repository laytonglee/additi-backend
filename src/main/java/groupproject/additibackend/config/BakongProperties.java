package groupproject.additibackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "bakong")
@Getter
@Setter
public class BakongProperties {
    
    private String token;
    // merchantId is not used for individual KHQR accounts, kept for reference only
    private String merchantId;
    private String merchantName;
    // Account ID format: "phone_number@bank" or "username@bank"
    // Examples: "012345678@aba", "012345678@wing", "username@bkrt"
    private String accountId;
    private String apiUrl = "https://api-bakong.nbc.gov.kh";
    private String currency = "USD"; // or "KHR"
}

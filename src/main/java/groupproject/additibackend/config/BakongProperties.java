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
    private String merchantId;
    private String merchantName;
    private String accountId; // e.g., "merchant@aba" or "merchant@wing"
    private String apiUrl = "https://api-bakong.nbc.gov.kh";
    private String currency = "USD"; // or "KHR"
}

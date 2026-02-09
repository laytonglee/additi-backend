package groupproject.additibackend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "cloudflare.r2")
@Getter
@Setter
@Slf4j
public class R2Config {

    private String accountId;
    private String accessKeyId;
    private String secretAccessKey;
    private String endpoint;
    private String bucketName;
    private String publicUrl;
    private String region;

    /**
     * Runs on startup to verify the length of your key exactly as Spring sees it.
     */
    @PostConstruct
    public void validateCredentials() {
        if (accessKeyId == null || accessKeyId.trim().length() != 32) {
            log.error("CRITICAL: R2 Access Key ID is INVALID. Current length: {}. Expected: 32.",
                    accessKeyId != null ? accessKeyId.trim().length() : "NULL");
        } else {
            log.info("R2 Credentials validated. AccessKey length: {}", accessKeyId.trim().length());
        }
    }

    @Bean
    public S3Client s3Client() {
        // Force trim to remove any invisible newline or space characters from .env
        String cleanAccessKey = (accessKeyId != null) ? accessKeyId.trim() : "";
        String cleanSecretKey = (secretAccessKey != null) ? secretAccessKey.trim() : "";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(cleanAccessKey, cleanSecretKey);

        return S3Client.builder()
                .region(Region.of(region != null ? region : "auto"))
                .endpointOverride(URI.create(endpoint))
                // StaticCredentialsProvider ensures it uses your code's keys, NOT system env variables
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        String cleanAccessKey = (accessKeyId != null) ? accessKeyId.trim() : "";
        String cleanSecretKey = (secretAccessKey != null) ? secretAccessKey.trim() : "";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(cleanAccessKey, cleanSecretKey);

        return S3Presigner.builder()
                .region(Region.of(region != null ? region : "auto"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}

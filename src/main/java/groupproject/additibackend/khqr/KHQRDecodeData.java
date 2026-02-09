package groupproject.additibackend.khqr;

import lombok.Data;

@Data
public class KHQRDecodeData {
    private String payloadFormatIndicator;
    private String pointOfInitiationMethod;
    private String merchantAccountInfo;
    private String bakongAccountId;
    private String acquiringBank;
    private String merchantCategoryCode;
    private String transactionCurrency;
    private String transactionAmount;
    private String countryCode;
    private String merchantName;
    private String merchantCity;
    private String billNumber;
    private String mobileNumber;
    private String storeLabel;
    private String terminalLabel;
    private String timestamp;
    private String crc;
    private boolean crcValid;
}

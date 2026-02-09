package groupproject.additibackend.khqr;

import lombok.Data;

@Data
public class IndividualInfo {
    private String bakongAccountId;
    private String accountInformation;
    private String acquiringBank;
    private KHQRCurrency currency = KHQRCurrency.USD;
    private Double amount;
    private String merchantName;
    private String merchantCity = "PHNOM PENH";
    private String billNumber;
    private String mobileNumber;
    private String storeLabel;
    private String terminalLabel;
}

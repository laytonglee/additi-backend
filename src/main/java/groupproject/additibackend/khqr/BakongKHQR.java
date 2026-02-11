package groupproject.additibackend.khqr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * BakongKHQR - KHQR Generator compatible with NBC Bakong SDK
 * This implementation generates KHQR codes following EMVCo QR Code Specification
 */
public class BakongKHQR {

    /**
     * Generate KHQR for Individual account
     * 
     * @param info IndividualInfo containing payment details
     * @return KHQRResponse with QR string and MD5 hash
     */
    public static KHQRResponse<KHQRData> generateIndividual(IndividualInfo info) {
        try {
            String qrString = buildQRString(info);
            String md5 = generateMD5(qrString);
            
            KHQRData data = new KHQRData(qrString, md5);
            return KHQRResponse.success(data);
            
        } catch (Exception e) {
            return KHQRResponse.error("Failed to generate KHQR: " + e.getMessage());
        }
    }

    private static String buildQRString(IndividualInfo info) {
        StringBuilder qr = new StringBuilder();
        
        // Payload Format Indicator (ID 00) - Required
        qr.append("000201");
        
        // Point of Initiation Method (ID 01) - 12 = Dynamic QR (one-time use)
        qr.append("010212");
        
        // Merchant Account Information (ID 29 for Individual)
        String merchantAccountInfo = buildMerchantAccountInfo(info);
        qr.append("29").append(formatLength(merchantAccountInfo.length())).append(merchantAccountInfo);
        
        // Merchant Category Code (ID 52) - 5999 = Miscellaneous
        qr.append("52045999");
        
        // Transaction Currency (ID 53) - Required
        qr.append("5303").append(info.getCurrency().getCode());
        
        // Transaction Amount (ID 54)
        if (info.getAmount() != null && info.getAmount() > 0) {
            // Format amount - MUST use Locale.US to ensure decimal point (.) not comma (,)
            String amount = String.format(Locale.US, "%.0f", info.getAmount());
            if (info.getAmount() != Math.floor(info.getAmount())) {
                amount = String.format(Locale.US, "%.2f", info.getAmount());
            }
            qr.append("54").append(formatLength(amount.length())).append(amount);
        }
        
        // Country Code (ID 58) - Required
        qr.append("5802KH");
        
        // Merchant Name (ID 59) - Required
        String merchantName = info.getMerchantName() != null ? info.getMerchantName() : "Merchant";
        if (merchantName.length() > 25) {
            merchantName = merchantName.substring(0, 25);
        }
        qr.append("59").append(formatLength(merchantName.length())).append(merchantName);
        
        // Merchant City (ID 60) - Required
        String city = info.getMerchantCity() != null ? info.getMerchantCity() : "PHNOM PENH";
        qr.append("60").append(formatLength(city.length())).append(city);
        
        // Additional Data Field Template (ID 62)
        String additionalData = buildAdditionalData(info);
        if (!additionalData.isEmpty()) {
            qr.append("62").append(formatLength(additionalData.length())).append(additionalData);
        }
        
        // Timestamp (ID 99)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String timestampField = "00" + formatLength(timestamp.length()) + timestamp;
        qr.append("99").append(formatLength(timestampField.length())).append(timestampField);
        
        // CRC (ID 63) - Required, placeholder
        qr.append("6304");
        
        // Calculate and append CRC16-CCITT
        String crc = calculateCRC16(qr.toString());
        qr.append(crc);
        
        // Debug output
        System.out.println("=== KHQR Generated ===");
        System.out.println("Account: " + info.getBakongAccountId());
        System.out.println("Amount: " + info.getAmount() + " " + info.getCurrency());
        System.out.println("Merchant: " + merchantName);
        System.out.println("QR String: " + qr.toString());
        System.out.println("QR Length: " + qr.length());
        System.out.println("=====================");
        
        return qr.toString();
    }

    private static String buildMerchantAccountInfo(IndividualInfo info) {
        StringBuilder mai = new StringBuilder();
        
        // According to Bakong KHQR SDK documentation:
        // Tag 00 - Bakong Account ID (e.g., "john_smith@devb")
        String accountId = info.getBakongAccountId();
        if (accountId != null && !accountId.isEmpty()) {
            mai.append("00").append(formatLength(accountId.length())).append(accountId);
        }
        
        // Tag 01 - Account Information (phone number)
        if (info.getAccountInformation() != null && !info.getAccountInformation().isEmpty()) {
            mai.append("01").append(formatLength(info.getAccountInformation().length())).append(info.getAccountInformation());
        }
        
        // Tag 02 - Acquiring Bank
        if (info.getAcquiringBank() != null && !info.getAcquiringBank().isEmpty()) {
            mai.append("02").append(formatLength(info.getAcquiringBank().length())).append(info.getAcquiringBank());
        }
        
        return mai.toString();
    }

    private static String buildAdditionalData(IndividualInfo info) {
        StringBuilder ad = new StringBuilder();
        
        // Bill Number (Tag 01)
        if (info.getBillNumber() != null && !info.getBillNumber().isEmpty()) {
            ad.append("01").append(formatLength(info.getBillNumber().length())).append(info.getBillNumber());
        }
        
        // Mobile Number (Tag 02)
        if (info.getMobileNumber() != null && !info.getMobileNumber().isEmpty()) {
            ad.append("02").append(formatLength(info.getMobileNumber().length())).append(info.getMobileNumber());
        }
        
        // Store Label (Tag 03)
        if (info.getStoreLabel() != null && !info.getStoreLabel().isEmpty()) {
            ad.append("03").append(formatLength(info.getStoreLabel().length())).append(info.getStoreLabel());
        }
        
        // Terminal Label (Tag 07)
        if (info.getTerminalLabel() != null && !info.getTerminalLabel().isEmpty()) {
            ad.append("07").append(formatLength(info.getTerminalLabel().length())).append(info.getTerminalLabel());
        }
        
        return ad.toString();
    }

    private static String formatLength(int length) {
        return String.format("%02d", length);
    }

    private static String calculateCRC16(String data) {
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

    private static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * Verify KHQR CRC validity
     * 
     * @param qrCode The KHQR string to verify
     * @return KHQRResponse with CRCValidation result
     */
    public static KHQRResponse<CRCValidation> verify(String qrCode) {
        try {
            if (qrCode == null || qrCode.length() < 8) {
                return KHQRResponse.error("Invalid KHQR code");
            }
            
            // CRC is the last 4 characters
            String actualCRC = qrCode.substring(qrCode.length() - 4).toUpperCase();
            
            // Calculate expected CRC (without the CRC value itself)
            String dataWithoutCRC = qrCode.substring(0, qrCode.length() - 4);
            String expectedCRC = calculateCRC16(dataWithoutCRC);
            
            boolean isValid = expectedCRC.equalsIgnoreCase(actualCRC);
            
            CRCValidation validation = new CRCValidation(isValid, expectedCRC, actualCRC);
            return KHQRResponse.success(validation);
            
        } catch (Exception e) {
            return KHQRResponse.error("Failed to verify KHQR: " + e.getMessage());
        }
    }

    /**
     * Decode KHQR string into structured data
     * 
     * @param qrCode The KHQR string to decode
     * @return KHQRResponse with decoded KHQR data
     */
    public static KHQRResponse<KHQRDecodeData> decode(String qrCode) {
        try {
            if (qrCode == null || qrCode.length() < 8) {
                return KHQRResponse.error("Invalid KHQR code");
            }
            
            KHQRDecodeData data = new KHQRDecodeData();
            
            // Verify CRC first
            KHQRResponse<CRCValidation> crcResponse = verify(qrCode);
            if (crcResponse.getKHQRStatus().getCode() == 0) {
                data.setCrcValid(crcResponse.getData().isValid());
                data.setCrc(crcResponse.getData().getActualCRC());
            }
            
            int index = 0;
            while (index < qrCode.length() - 4) { // -4 for CRC
                if (index + 4 > qrCode.length()) break;
                
                String tagId = qrCode.substring(index, index + 2);
                int length = Integer.parseInt(qrCode.substring(index + 2, index + 4));
                
                if (index + 4 + length > qrCode.length()) break;
                
                String value = qrCode.substring(index + 4, index + 4 + length);
                
                switch (tagId) {
                    case "00" -> data.setPayloadFormatIndicator(value);
                    case "01" -> data.setPointOfInitiationMethod(value);
                    case "29", "30" -> {
                        data.setMerchantAccountInfo(value);
                        // Parse sub-fields
                        parseMerchantAccountInfo(value, data);
                    }
                    case "52" -> data.setMerchantCategoryCode(value);
                    case "53" -> data.setTransactionCurrency(value);
                    case "54" -> data.setTransactionAmount(value);
                    case "58" -> data.setCountryCode(value);
                    case "59" -> data.setMerchantName(value);
                    case "60" -> data.setMerchantCity(value);
                    case "62" -> parseAdditionalData(value, data);
                    case "99" -> data.setTimestamp(value);
                }
                
                index += 4 + length;
            }
            
            return KHQRResponse.success(data);
            
        } catch (Exception e) {
            return KHQRResponse.error("Failed to decode KHQR: " + e.getMessage());
        }
    }

    private static void parseMerchantAccountInfo(String value, KHQRDecodeData data) {
        int index = 0;
        while (index < value.length()) {
            if (index + 4 > value.length()) break;
            
            String tagId = value.substring(index, index + 2);
            int length = Integer.parseInt(value.substring(index + 2, index + 4));
            
            if (index + 4 + length > value.length()) break;
            
            String subValue = value.substring(index + 4, index + 4 + length);
            
            switch (tagId) {
                case "00" -> {} // GUID
                case "01" -> data.setBakongAccountId(subValue);
                case "02" -> data.setAcquiringBank(subValue);
            }
            
            index += 4 + length;
        }
    }

    private static void parseAdditionalData(String value, KHQRDecodeData data) {
        int index = 0;
        while (index < value.length()) {
            if (index + 4 > value.length()) break;
            
            String tagId = value.substring(index, index + 2);
            int length = Integer.parseInt(value.substring(index + 2, index + 4));
            
            if (index + 4 + length > value.length()) break;
            
            String subValue = value.substring(index + 4, index + 4 + length);
            
            switch (tagId) {
                case "01" -> data.setBillNumber(subValue);
                case "02" -> data.setMobileNumber(subValue);
                case "03" -> data.setStoreLabel(subValue);
                case "07" -> data.setTerminalLabel(subValue);
            }
            
            index += 4 + length;
        }
    }
}

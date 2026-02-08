package groupproject.additibackend.service;

import java.math.BigDecimal;

import groupproject.additibackend.model.Order;

public interface BakongService {
    
    /**
     * KHQR result containing both QR string and MD5 hash for verification
     */
    record KHQRResult(String qr, String md5Hash) {}
    
    /**
     * Generate a KHQR code for payment
     * @param order The order to generate payment for
     * @return KHQRResult with both QR string and MD5 hash
     */
    KHQRResult generateKHQR(Order order);
    
    /**
     * Generate KHQR with custom amount
     * @param amount Payment amount
     * @param currency USD or KHR
     * @param transactionId Unique transaction reference
     * @return KHQRResult with both QR string and MD5 hash
     */
    KHQRResult generateKHQR(BigDecimal amount, String currency, String transactionId);
    
    /**
     * Verify if a payment was successful
     * @param transactionId The transaction ID to verify
     * @return true if payment was successful
     */
    boolean verifyPayment(String transactionId);
    
    /**
     * Check payment status
     * @param md5Hash MD5 hash of the KHQR string
     * @return Payment status details
     */
    PaymentStatusResponse checkPaymentStatus(String md5Hash);
    
    record PaymentStatusResponse(
        boolean paid,
        String transactionId,
        BigDecimal amount,
        String currency,
        String payerAccount,
        String timestamp
    ) {}
}

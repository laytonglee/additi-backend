package groupproject.additibackend.service;

import java.math.BigDecimal;

import groupproject.additibackend.model.Order;

public interface BakongService {
    
    /**
     * Generate a KHQR code for payment
     * @param order The order to generate payment for
     * @return The KHQR string (to be converted to QR image on frontend)
     */
    String generateKHQR(Order order);
    
    /**
     * Generate KHQR with custom amount
     * @param amount Payment amount
     * @param currency USD or KHR
     * @param transactionId Unique transaction reference
     * @return The KHQR string
     */
    String generateKHQR(BigDecimal amount, String currency, String transactionId);
    
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

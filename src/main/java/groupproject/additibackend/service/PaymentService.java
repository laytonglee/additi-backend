package groupproject.additibackend.service;

import groupproject.additibackend.model.Payment;
import groupproject.additibackend.request.PaymentRequest;

public interface PaymentService {
    Payment createPayment(Long orderId, PaymentRequest request);
    Payment getPaymentById(Long paymentId);
    Payment processKHQRPayment(Long orderId, String khqrCode);
    Payment processCashOnDelivery(Long orderId);
    Payment verifyPayment(Long paymentId);
}

package groupproject.additibackend.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import groupproject.additibackend.model.Order;
import groupproject.additibackend.model.Payment;
import groupproject.additibackend.repository.OrderRepository;
import groupproject.additibackend.repository.PaymentRepository;
import groupproject.additibackend.request.PaymentRequest;
import groupproject.additibackend.service.BakongService;

@Service
public class PaymentServiceImpl implements groupproject.additibackend.service.PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final BakongService bakongService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, BakongService bakongService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.bakongService = bakongService;
    }

    @Override
    @Transactional
    public Payment createPayment(Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(Payment.PaymentMethod.valueOf(request.getMethod()));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    @Transactional
    public Payment processKHQRPayment(Long orderId, String khqrCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Generate real KHQR code using Bakong API
        BakongService.KHQRResult khqrResult = bakongService.generateKHQR(order);
        
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(Payment.PaymentMethod.KHQR);
        payment.setKhqrCode(khqrResult.qr());
        payment.setMd5Hash(khqrResult.md5Hash()); // Save MD5 hash for verification
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Don't confirm order until payment is verified
        // Order stays in PENDING until webhook confirms payment

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment processCashOnDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(Payment.PaymentMethod.CASH_ON_DELIVERY);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update order status to CONFIRMED for cash on delivery
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment verifyPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        
        log.info("Verifying payment: id={}, method={}, md5Hash={}", 
            paymentId, payment.getMethod(), payment.getMd5Hash());

        // For KHQR payments, verify with Bakong API using MD5 hash
        if (payment.getMethod() == Payment.PaymentMethod.KHQR && payment.getMd5Hash() != null) {
            BakongService.PaymentStatusResponse status = bakongService.checkPaymentStatus(payment.getMd5Hash());
            
            log.info("Payment verification result: paid={}", status.paid());
            
            if (status.paid()) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Update order status to CONFIRMED after payment verified
                Order order = updatedPayment.getOrder();
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
                
                log.info("Payment {} completed successfully!", paymentId);
                return updatedPayment;
            } else {
                // Payment not yet received
                log.info("Payment {} still pending", paymentId);
                return payment;
            }
        }
        
        // For cash on delivery, just mark as completed when verified
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update order status
        Order order = updatedPayment.getOrder();
        order.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(order);

        return updatedPayment;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString();
    }
}

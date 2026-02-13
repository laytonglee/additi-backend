package groupproject.additibackend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import groupproject.additibackend.model.Order;
import groupproject.additibackend.model.Payment;
import groupproject.additibackend.model.User;
import groupproject.additibackend.request.CheckoutRequest;
import groupproject.additibackend.request.PaymentRequest;
import groupproject.additibackend.response.OrderResponse;
import groupproject.additibackend.response.PaymentResponse;
import groupproject.additibackend.service.OrderService;
import groupproject.additibackend.service.PaymentService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @RequestBody CheckoutRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Order order = orderService.createOrderFromCart(user, request);
        OrderResponse response = convertToOrderResponse(order);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Order> orders = orderService.getUserOrders(user);
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        OrderResponse response = convertToOrderResponse(order);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(orderId, request);
        PaymentResponse response = convertToPaymentResponse(payment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/payment/khqr")
    public ResponseEntity<PaymentResponse> processKHQRPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processKHQRPayment(orderId, request.getKhqrCode());
        PaymentResponse response = convertToPaymentResponse(payment);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{orderId}/payment/cash")
    public ResponseEntity<PaymentResponse> processCashOnDelivery(
            @PathVariable Long orderId) {
        Payment payment = paymentService.processCashOnDelivery(orderId);
        PaymentResponse response = convertToPaymentResponse(payment);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Verify if KHQR payment has been completed.
     * Frontend should poll this endpoint after displaying QR code.
     */
    @GetMapping("/{orderId}/payment/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable Long orderId,
            @PathVariable Long paymentId) {
        Payment payment = paymentService.verifyPayment(paymentId);
        PaymentResponse response = convertToPaymentResponse(payment);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        Order order = orderService.updateOrderStatus(orderId, Order.OrderStatus.valueOf(status));
        OrderResponse response = convertToOrderResponse(order);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().toString());
        response.setShippingAddress(order.getShippingAddress());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        // Convert order items to response DTOs
        if (order.getOrderItems() != null) {
            response.setItems(order.getOrderItems().stream().map(item -> {
                OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setProductId(item.getProduct().getId());
                itemResponse.setProductName(item.getProduct().getName());
                if (item.getProductVariant() != null) {
                    itemResponse.setProductVariantId(item.getProductVariant().getId());
                }
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setPrice(item.getPrice());
                itemResponse.setSubtotal(item.getSubtotal());
                return itemResponse;
            }).collect(Collectors.toList()));
        }
        
        return response;
    }

    private PaymentResponse convertToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setMethod(payment.getMethod().toString());
        response.setStatus(payment.getStatus().toString());
        response.setAmount(payment.getAmount());
        response.setTransactionId(payment.getTransactionId());
        response.setKhqrCode(payment.getKhqrCode());
        response.setMd5Hash(payment.getMd5Hash());
        response.setCreatedAt(payment.getCreatedAt());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }
}


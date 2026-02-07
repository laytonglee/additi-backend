package groupproject.additibackend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import groupproject.additibackend.model.Order;
import groupproject.additibackend.model.OrderItem;
import groupproject.additibackend.model.Product;
import groupproject.additibackend.model.ProductVariant;
import groupproject.additibackend.model.User;
import groupproject.additibackend.repository.OrderRepository;
import groupproject.additibackend.repository.ProductRepository;
import groupproject.additibackend.repository.ProductVariantRepository;
import groupproject.additibackend.request.CheckoutRequest;

@Service
public class OrderServiceImpl implements groupproject.additibackend.service.OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public OrderServiceImpl(OrderRepository orderRepository, 
                           ProductRepository productRepository,
                           ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(User user, CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Convert cart items from request to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CheckoutRequest.CartItemRequest cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            ProductVariant variant = null;
            BigDecimal price = product.getPrice();

            if (cartItem.getProductVariantId() != null) {
                variant = productVariantRepository.findById(cartItem.getProductVariantId())
                        .orElseThrow(() -> new RuntimeException("Product variant not found: " + cartItem.getProductVariantId()));
                if (variant.getPriceAdjustment() != null) {
                    price = price.add(variant.getPriceAdjustment());
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(price);
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.calculateTotal();

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Override
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
}

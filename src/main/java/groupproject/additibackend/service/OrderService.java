package groupproject.additibackend.service;

import groupproject.additibackend.model.Order;
import groupproject.additibackend.model.User;
import groupproject.additibackend.request.CheckoutRequest;

import java.util.List;

public interface OrderService {
    Order createOrderFromCart(User user, CheckoutRequest request);
    Order getOrderById(Long orderId);
    Order getOrderByIdForUser(Long orderId, Long userId);
    List<Order> getUserOrders(User user);
    List<Order> getAllOrders();
    List<Order> getAllOrdersByUserId(Long userId);
    Order updateOrderStatus(Long orderId, Order.OrderStatus status);
}

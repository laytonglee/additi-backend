package groupproject.additibackend.service;

import groupproject.additibackend.model.Cart;
import groupproject.additibackend.model.CartItem;
import groupproject.additibackend.model.User;

public interface CartService {
    Cart getOrCreateCart(User user);
    CartItem addToCart(User user, Long productId, Long productVariantId, Integer quantity);
    void removeFromCart(Long cartItemId);
    Cart getCart(User user);
    void clearCart(Long cartId);
    CartItem updateCartItemQuantity(Long cartItemId, Integer quantity);
}

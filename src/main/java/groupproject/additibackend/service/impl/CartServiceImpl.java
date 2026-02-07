package groupproject.additibackend.service.impl;

import groupproject.additibackend.model.*;
import groupproject.additibackend.repository.CartRepository;
import groupproject.additibackend.repository.ProductReponsitory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CartServiceImpl implements groupproject.additibackend.service.CartService {

    private final CartRepository cartRepository;
    private final ProductReponsitory productRepository;

    public CartServiceImpl(CartRepository cartRepository, ProductReponsitory productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartItem addToCart(User user, Long productId, Long productVariantId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice());

        if (productVariantId != null) {
            // Load product variant if needed (would require ProductVariantRepository)
        }

        cart.getItems().add(cartItem);
        cart.calculateTotal();
        cartRepository.save(cart);

        return cartItem;
    }

    @Override
    public void removeFromCart(Long cartItemId) {
        // Would need CartItemRepository to implement this
        // This is a simplified version
    }

    @Override
    public Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));
    }

    @Override
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        // Would need CartItemRepository to implement this
        return null;
    }
}

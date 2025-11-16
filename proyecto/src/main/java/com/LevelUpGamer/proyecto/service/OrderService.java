package com.LevelUpGamer.proyecto.service;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.OrderRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    /**
     * El método principal de Checkout.
     * Es "Transaccional": si algo falla (ej. al guardar el pedido),
     * no se guardarán los cambios en el usuario (puntos) ni se borrará el carrito.
     */
    @Transactional
    public Order createOrderFromCart(String username) {

        // 1. OBTENER USUARIO Y CARRITO
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 2. CALCULAR PRECIOS
        double originalPrice = cart.getTotalPrice();
        double finalPrice = originalPrice;
        int pointsEarned = 0;

        // 3. APLICAR LÓGICA DE DUOC (¡TU REGLA DE NEGOCIO!)
        if (user.getUserRole() != null && user.getUserRole().equals("ROLE_DUOC")) {
            // Aplica 20% de descuento
            finalPrice = originalPrice * 0.80;

            // Calcula puntos (¡TU FÓRMULA!)
            pointsEarned = (int) (Math.floor(finalPrice / 1000) * 10);

            user.setPointsBalance(user.getPointsBalance() + pointsEarned);
            // Añade puntos al "historial" (para nivel)
            user.setTotalPointsEarned(user.getTotalPointsEarned() + pointsEarned); // <-- ¡AÑADE ESTO!

            // Actualiza el nivel (ahora usará el total)
            updateUserLevel(user);
        }

        // 4. CREAR EL PEDIDO (LA "FACTURA")
        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setOriginalPrice(originalPrice);
        newOrder.setFinalPrice(finalPrice);
        newOrder.setPointsEarned(pointsEarned);

        // 5. COPIAR ITEMS DEL CARRITO AL PEDIDO
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice()); // Guarda el precio de ese momento

            newOrder.getOrderItems().add(orderItem);
        }

        // 6. VACIAR EL CARRITO
        // 'orphanRemoval=true' y 'cascade=ALL' en Cart.java se encargarían
        // de los CartItems si borramos el carrito.
        // Pero es más seguro solo vaciar la lista:
        cart.getCartItems().clear();

        // 7. GUARDAR TODO EN LA BD
        cartRepository.save(cart);      // Guarda el carrito (ahora vacío)
        userRepository.save(user);      // Guarda el usuario (con nuevos puntos/nivel)
        Order savedOrder = orderRepository.save(newOrder); // Guarda el pedido (y sus items)

        return savedOrder;
    }

    /**
     * Lógica de niveles (Total: 4 niveles).
     * Nivel 1: 0-999
     * Nivel 2: 1000-4999
     * Nivel 3: 5000-9999
     * Nivel 4: 10000+
     */
    private void updateUserLevel(User user) {
        int points = user.getTotalPointsEarned();

        if (points >= 150000) {
            user.setUserLevel(4);
        } else if (points >= 100000) {
            user.setUserLevel(3);
        } else if (points >= 50000) {
            user.setUserLevel(2);
        } else {
            user.setUserLevel(1); // Nivel por defecto
        }
    }
}
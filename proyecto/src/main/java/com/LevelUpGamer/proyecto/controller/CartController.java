package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.dto.CartItemRequest;
import com.LevelUpGamer.proyecto.model.Cart;
import com.LevelUpGamer.proyecto.model.CartItem;
import com.LevelUpGamer.proyecto.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // <-- ¡NUEVA IMPORTACIÓN!
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Obtiene el carrito del usuario actualmente autenticado.
     * Petición: GET http://localhost:8081/api/cart
     */
    @GetMapping
    public ResponseEntity<Cart> getMyCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return cartRepository.findByUserUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Añade un producto al carrito del usuario (o suma la cantidad si ya existe).
     * Petición: POST http://localhost:8081/api/cart/items
     * Body: { "productId": 1, "quantity": 1 }
     */
    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(@RequestBody CartItemRequest itemRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));
        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(itemRequest.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + itemRequest.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(itemRequest.getQuantity());
            cart.getCartItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(savedCart);
    }

    /**
     * --- ¡NUEVO MÉTODO! ---
     * Actualiza la cantidad de un item en el carrito (la "establece").
     * Petición: PUT http://localhost:8081/api/cart/items/5
     * (Donde 5 es el ID del *Producto*)
     * Body: { "quantity": 3 }
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> payload) { // Recibe un JSON simple: {"quantity": N}

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        Integer newQuantity = payload.get("quantity");
        if (newQuantity == null || newQuantity < 0) {
            return ResponseEntity.badRequest().body(null); // Cantidad inválida
        }

        Optional<CartItem> itemToUpdate = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemToUpdate.isPresent()) {
            if (newQuantity == 0) {
                // Si la cantidad es 0, lo eliminamos
                cart.getCartItems().remove(itemToUpdate.get());
            } else {
                // Si es > 0, actualizamos la cantidad
                itemToUpdate.get().setQuantity(newQuantity);
            }
            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } else {
            return ResponseEntity.notFound().build(); // Producto no encontrado en el carrito
        }
    }


    /**
     * --- ¡MÉTODO CORREGIDO! ---
     * Elimina un item del carrito usando el ID del Producto.
     * Petición: DELETE http://localhost:8081/api/cart/items/5
     * (Donde 5 es el ID del *Producto*, no del CartItem)
     */
    @DeleteMapping("/items/{productId}") // CAMBIADO: de {cartItemId} a {productId}
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable Long productId) { // CAMBIADO: de cartItemId a productId
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // CAMBIADO: Buscar por productId en lugar de cartItemId
        Optional<CartItem> itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemToRemove.isPresent()) {
            cart.getCartItems().remove(itemToRemove.get());
            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * --- ¡NUEVO MÉTODO! ---
     * Vacía por completo el carrito del usuario.
     * Petición: DELETE http://localhost:8081/api/cart
     */
    @DeleteMapping
    public ResponseEntity<Cart> clearCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        cart.getCartItems().clear(); // Elimina todos los items de la lista

        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(savedCart);
    }
}
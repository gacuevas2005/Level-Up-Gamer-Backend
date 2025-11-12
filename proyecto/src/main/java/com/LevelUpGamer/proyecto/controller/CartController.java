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
     * (Requiere token JWT)
     */
    @GetMapping
    public ResponseEntity<Cart> getMyCart() {
        // Obtenemos el nombre de usuario del contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Buscamos el carrito por el nombre de usuario
        return cartRepository.findByUserUsername(username)
                .map(ResponseEntity::ok) // Si se encuentra, devuelve 200 OK con el carrito
                .orElse(ResponseEntity.notFound().build()); // Si no, 404
    }

    /**
     * Añade un producto al carrito del usuario.
     * Si el producto ya existe, actualiza la cantidad.
     * Petición: POST http://localhost:8081/api/cart/items
     * Body: { "productId": 1, "quantity": 1 }
     * (Requiere token JWT)
     */
    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(@RequestBody CartItemRequest itemRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Obtener el carrito y el producto
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 2. Verificar si el producto ya está en el carrito
        // --- ¡CORREGIDO! --- (Era getItems())
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(itemRequest.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Si existe, actualiza la cantidad
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + itemRequest.getQuantity());
        } else {
            // Si no existe, crea un nuevo CartItem
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(itemRequest.getQuantity());
            // Añade el nuevo item a la lista del carrito
            cart.getCartItems().add(newItem);
        }

        // 3. Guarda el carrito (gracias a CascadeType.ALL, esto guarda/actualiza los items)
        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(savedCart);
    }

    /**
     * Elimina un item del carrito.
     * Petición: DELETE http://localhost:8081/api/cart/items/5
     * (Donde 5 es el ID del *CartItem*, no del producto)
     * (Requiere token JWT)
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable Long cartItemId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // Busca el item *dentro* de la lista del carrito
        // --- ¡CORREGIDO! --- (Era getItems())
        Optional<CartItem> itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst();

        if (itemToRemove.isPresent()) {
            // Si se encuentra, remuévelo de la lista
            // (Gracias a orphanRemoval=true, esto lo borrará de la BD)
            // --- ¡CORREGIDO! --- (Era getItems())
            cart.getCartItems().remove(itemToRemove.get());
            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } else {
            // Si no se encontró ese item en el carrito del usuario
            return ResponseEntity.notFound().build();
        }
    }
}

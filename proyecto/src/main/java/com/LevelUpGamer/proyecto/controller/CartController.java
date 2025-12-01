package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.dto.CartItemRequest;
import com.LevelUpGamer.proyecto.model.Cart;
import com.LevelUpGamer.proyecto.model.CartItem;
import com.LevelUpGamer.proyecto.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrito de Compras", description = "Operaciones para gestionar el carrito de compras del usuario (ver, añadir, actualizar cantidad, eliminar).")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Obtiene el carrito del usuario actual.
     */
    @Operation(summary = "Obtener mi carrito", description = "Devuelve el carrito actual del usuario logueado con todos sus productos y el precio total calculado.")
    @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente")
    @GetMapping
    public ResponseEntity<Cart> getMyCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return cartRepository.findByUserUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Añade un producto al carrito.
     */
    @Operation(summary = "Añadir producto al carrito", description = "Añade un producto nuevo al carrito. Si el producto ya existe, suma la cantidad indicada a la existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto añadido/actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "El producto no existe")
    })
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
     * Actualiza la cantidad exacta de un item.
     */
    @Operation(summary = "Actualizar cantidad de un producto", description = "Establece una cantidad específica para un producto en el carrito. Si la cantidad es 0, elimina el producto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito")
    })
    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @Parameter(description = "ID del Producto a actualizar") @PathVariable Long productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON con la nueva cantidad. Ejemplo: {\"quantity\": 3}",
                    content = @Content(examples = @ExampleObject(value = "{\"quantity\": 3}"))
            )
            @RequestBody Map<String, Integer> payload) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        Integer newQuantity = payload.get("quantity");
        if (newQuantity == null || newQuantity < 0) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<CartItem> itemToUpdate = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemToUpdate.isPresent()) {
            if (newQuantity == 0) {
                cart.getCartItems().remove(itemToUpdate.get());
            } else {
                itemToUpdate.get().setQuantity(newQuantity);
            }
            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un producto del carrito.
     */
    @Operation(summary = "Eliminar producto del carrito", description = "Elimina completamente un producto del carrito, independientemente de la cantidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "El producto no estaba en el carrito")
    })
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @Parameter(description = "ID del Producto a eliminar") @PathVariable Long productId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

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
     * Vacía el carrito completo.
     */
    @Operation(summary = "Vaciar carrito", description = "Elimina TODOS los productos del carrito del usuario.")
    @ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente")
    @DeleteMapping
    public ResponseEntity<Cart> clearCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = cartRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        cart.getCartItems().clear();

        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(savedCart);
    }
}
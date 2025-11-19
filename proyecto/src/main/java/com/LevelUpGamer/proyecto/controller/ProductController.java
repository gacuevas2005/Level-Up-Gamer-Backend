package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.model.Product;
import com.LevelUpGamer.proyecto.model.Review;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Catálogo de Productos", description = "Endpoints para ver productos, crear nuevos items (admin) y gestionar reseñas de usuarios.")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * 1. OBTENER TODOS los productos.
     */
    @Operation(summary = "Obtener catálogo completo", description = "Devuelve una lista con todos los productos disponibles en la base de datos.")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
    @GetMapping
    public List<Product> getAllProducts() {
        System.out.println("Solicitud recibida: Devolviendo todo el catálogo de productos.");
        return productRepository.findAll();
    }

    /**
     * 2. OBTENER UN producto por su ID.
     */
    @Operation(summary = "Obtener producto por ID", description = "Busca un producto específico por su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID del producto a buscar") @PathVariable Long id) {

        return productRepository.findById(id)
                .map(product -> {
                    System.out.println("Enviando producto: " + product.getName());
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    System.out.println("Error: Producto con id " + id + " no encontrado.");
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 3. CREAR un nuevo producto.
     */
    @Operation(summary = "Crear nuevo producto", description = "Guarda un nuevo producto en la base de datos. (Idealmente restringido a administradores).")
    @ApiResponse(responseCode = "200", description = "Producto creado exitosamente")
    @PostMapping
    public Product createProduct(@RequestBody Product newProduct) {
        System.out.println("Solicitud recibida: Creando nuevo producto - " + newProduct.getName());
        return productRepository.save(newProduct);
    }

    /**
     * 4. AÑADIR UNA NUEVA RESEÑA (Versión Segura)
     */
    @Operation(summary = "Añadir reseña a un producto", description = "Permite a un usuario autenticado dejar una reseña en un producto. El autor se toma automáticamente del token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña guardada exitosamente"),
            @ApiResponse(responseCode = "404", description = "El producto al que intentas reseñar no existe"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Token inválido o faltante)")
    })
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<Review> addReviewToProduct(
            @Parameter(description = "ID del producto a reseñar") @PathVariable Long productId,
            @RequestBody Review newReview) {

        // 1. Obtenemos la autenticación (gracias al JwtAuthFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Obtenemos el username
        String currentUsername = authentication.getName();

        // 3. ¡Establecemos el autor en la reseña automáticamente!
        newReview.setAuthor(currentUsername);

        // Paso A: Busca el producto
        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            System.out.println("Error: No se encontró el producto con id " + productId);
            return ResponseEntity.notFound().build();
        }

        // Paso C: Enlaza la reseña con el producto
        newReview.setProduct(productOptional.get());

        // Paso D: Guarda la reseña
        Review savedReview = reviewRepository.save(newReview);

        System.out.println("Reseña guardada por '" + currentUsername + "' para el producto: " + productOptional.get().getName());
        return ResponseEntity.ok(savedReview);
    }
}
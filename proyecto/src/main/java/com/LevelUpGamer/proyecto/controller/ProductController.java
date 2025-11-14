package com.LevelUpGamer.proyecto.controller;
import com.LevelUpGamer.proyecto.model.Product;
import com.LevelUpGamer.proyecto.model.Review;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder; // <-- AÑADE ESTE IMPORT
import org.springframework.security.core.Authentication; // <-- AÑADE ESTE IMPORT

import java.util.List;
import java.util.Optional;

@RestController // Indica que esto es un controlador REST (devuelve JSON)
@RequestMapping("/api/products") // URL base para todos los endpoints de este controlador
public class ProductController {

    // Inyección de dependencias: Spring nos "inyecta" una instancia
    // del repositorio para que podamos usarla.
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * 1. OBTENER TODOS los productos (el catálogo).
     * Petición: GET http://localhost:8081/api/products
     * @return Una lista de todos los productos en la base de datos.
     */
    @GetMapping
    public List<Product> getAllProducts() {
        // Mensaje en español para la consola del backend
        System.out.println("Solicitud recibida: Devolviendo todo el catálogo de productos.");
        return productRepository.findAll();
    }

    /**
     * 2. OBTENER UN producto por su ID.
     * Petición: GET http://localhost:8081/api/products/1 (para el ID 1)
     * @param id El ID del producto que viene en la URL
     * @return El producto si se encuentra, o un error 404 (Not Found) si no.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // Usamos .findById() que devuelve un Optional (puede o no encontrarlo)
        return productRepository.findById(id)
                .map(product -> {
                    // Si se encontró (map)
                    System.out.println("Enviando producto: " + product.getName());
                    return ResponseEntity.ok(product); // Devuelve 200 OK con el producto
                })
                .orElseGet(() -> {
                    // Si no se encontró (orElseGet)
                    System.out.println("Error: Producto con id " + id + " no encontrado.");
                    return ResponseEntity.notFound().build(); // Devuelve 404 Not Found
                });
    }

    /**
     * 3. CREAR un nuevo producto.
     * Petición: POST http://localhost:8081/api/products
     * @param newProduct El JSON del producto a crear, viene en el body de la petición.
     * @return El producto guardado (ya con su ID asignado por la BD).
     */
    @PostMapping
    public Product createProduct(@RequestBody Product newProduct) {
        // Mensaje en español para la consola
        System.out.println("Solicitud recibida: Creando nuevo producto - " + newProduct.getName());

        // El método .save() de JPA hace un 'INSERT' si el objeto no tiene ID.
        return productRepository.save(newProduct);
    }




    /**
     * 4. AÑADIR UNA NUEVA RESEÑA (Versión Segura)
     *  Petición: POST http://localhost:8081/api/products/1/reviews
     * Petición: POST /api/products/1/reviews (AHORA REQUIERE TOKEN)
     */
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<Review> addReviewToProduct(
            @PathVariable Long productId,
            @RequestBody Review newReview) { // El 'newReview' del frontend NO traerá 'author'

        // --- ¡AQUÍ ESTÁ LA MAGIA! ---
        // 1. Obtenemos la autenticación (gracias al JwtAuthFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Obtenemos el username (ej: "GamerPro")
        String currentUsername = authentication.getName();

        // 3. ¡Establecemos el autor en la reseña!
        // Ignoramos lo que sea que venga en newReview.setAuthor()
        newReview.setAuthor(currentUsername);
        // --- FIN DE LA MAGIA ---

        // Paso A: Busca el producto (esto no cambia)
        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            System.out.println("Error: No se encontró el producto con id " + productId);
            return ResponseEntity.notFound().build();
        }

        // Paso C: Enlaza la reseña con el producto (esto no cambia)
        newReview.setProduct(productOptional.get());

        // Paso D: Guarda la reseña (ahora con el autor correcto)
        Review savedReview = reviewRepository.save(newReview);

        System.out.println("Reseña guardada por '" + currentUsername + "' para el producto: " + productOptional.get().getName());
        return ResponseEntity.ok(savedReview);
    }
}

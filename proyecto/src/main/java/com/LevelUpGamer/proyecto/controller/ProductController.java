package com.LevelUpGamer.proyecto.controller;
import com.LevelUpGamer.proyecto.model.Product;
import com.LevelUpGamer.proyecto.model.Review;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * 4. AÑADIR UNA NUEVA RESEÑA a un producto específico.
     * Petición: POST http://localhost:8081/api/products/1/reviews
     * @param productId El ID del producto (viene de la URL)
     * @param newReview El JSON de la reseña (viene del Body)
     * @return La reseña guardada o un error 404 si el producto no existe.
     */
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<Review> addReviewToProduct(
            @PathVariable Long productId,
            @RequestBody Review newReview) {

        // Paso A: Busca el producto al que pertenece esta reseña
        Optional<Product> productOptional = productRepository.findById(productId);

        // Paso B: Comprueba si el producto existe
        if (productOptional.isEmpty()) {
            // Si no se encuentra el producto, devuelve un 404 Not Found
            System.out.println("Error: No se encontró el producto con id " + productId);
            return ResponseEntity.notFound().build();
        }

        // Paso C: ¡Enlaza la reseña con el producto!
        newReview.setProduct(productOptional.get());

        // Paso D: Guarda la NUEVA reseña en su propia tabla (ReviewRepository)
        Review savedReview = reviewRepository.save(newReview);

        System.out.println("Reseña guardada para el producto: " + productOptional.get().getName());
        return ResponseEntity.ok(savedReview); // Devuelve 200 OK con la reseña creada
    }
}
package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.model.Product;
import com.LevelUpGamer.proyecto.model.Review;
import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import com.LevelUpGamer.proyecto.service.FileStorageService; // Usamos tu servicio de archivos
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FilenameUtils; // Necesario para extensiones
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Gestión de Productos", description = "Catálogo público y administración de productos (CRUD completo con imágenes).")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // --- PÚBLICO: VER PRODUCTOS ---

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- ADMIN: CREAR PRODUCTO CON IMAGEN ---

    @Operation(summary = "Crear producto (Admin)", description = "Requiere Multipart/Form-Data. Sube la imagen al servidor y guarda la ruta en la BD.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "manufacturer", required = false) String manufacturer,
            @RequestParam("image") MultipartFile imageFile
    ) {
        try {
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setPrice(price);
            newProduct.setCategory(category);
            newProduct.setDescription(description);
            newProduct.setManufacturer(manufacturer);

            // Guardar Imagen
            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String filename = "prod_" + System.currentTimeMillis() + "." + extension;
            fileStorageService.store(imageFile, filename);

            // Generar URL pública
            String imageUrl = "http://localhost:8081/uploads/" + filename;
            newProduct.setImageUrl(imageUrl);

            Product savedProduct = productRepository.save(newProduct);
            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error al subir la imagen: " + e.getMessage());
        }
    }

    // --- ADMIN: ACTUALIZAR PRODUCTO ---

    @Operation(summary = "Actualizar producto (Admin)", description = "Actualiza datos. Si se envía una nueva imagen, reemplaza la anterior.")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        return productRepository.findById(id).map(product -> {
            try {
                product.setName(name);
                product.setPrice(price);
                product.setCategory(category);
                product.setDescription(description);

                // Solo actualizamos la imagen si el admin subió una nueva
                if (imageFile != null && !imageFile.isEmpty()) {
                    String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
                    String filename = "prod_" + id + "_" + System.currentTimeMillis() + "." + extension;
                    fileStorageService.store(imageFile, filename);

                    String imageUrl = "http://localhost:8081/uploads/" + filename;
                    product.setImageUrl(imageUrl);
                }

                return ResponseEntity.ok(productRepository.save(product));
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("Error al procesar imagen");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- ADMIN: ELIMINAR PRODUCTO ---

    @Operation(summary = "Eliminar producto (Admin)", description = "Elimina un producto de la base de datos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Producto eliminado correctamente");
    }

    // --- USUARIO: AÑADIR RESEÑA ---

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<Review> addReviewToProduct(
            @PathVariable Long productId,
            @RequestBody Review newReview) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        newReview.setAuthor(currentUsername);

        return productRepository.findById(productId).map(product -> {
            newReview.setProduct(product);
            return ResponseEntity.ok(reviewRepository.save(newReview));
        }).orElse(ResponseEntity.notFound().build());
    }
}
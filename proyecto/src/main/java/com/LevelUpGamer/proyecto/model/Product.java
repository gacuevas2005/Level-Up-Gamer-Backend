package com.LevelUpGamer.proyecto.model;
import jakarta.persistence.*; // Asegúrate de usar jakarta si es Spring Boot 3+
import lombok.Data; // Lombok para ahorrarnos getters/setters
import java.util.List;
import java.util.ArrayList;

@Data // <-- Anotación de Lombok: genera getters, setters, toString, etc.
@Entity // <-- Le dice a JPA que esta clase es una tabla en la BD
@Table(name = "products") // Nombre de la tabla en MySQL
public class Product {

    @Id // Marca esto como la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID Autoincrementable
    private Long id;

    @Column(nullable = false) // No puede ser nulo
    private String name; // Nombre

    @Column(nullable = false)
    private Double price; // Precio

    private String category; // Categoría

    @Column(name = "image_url") // Nombre de la columna en la BD
    private String imageUrl; // URL de la imagen

    @Column(columnDefinition = "TEXT") // Para descripciones largas
    private String description; // Descripción

    private String manufacturer; // Manofactura

    private String distributor; // Distribuidor

    /*
     * RELACIÓN: Un Producto (Product) puede tener muchas Reseñas (Review).
     * - 'mappedBy="product"': Le dice a JPA que la entidad 'Review' maneja
     * la relación (ahí estará el @ManyToOne).
     * - 'cascade=CascadeType.ALL': Si borramos un producto, se borran sus reseñas.
     * - 'fetch=FetchType.LAZY': No cargues las reseñas a menos que las pidamos.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
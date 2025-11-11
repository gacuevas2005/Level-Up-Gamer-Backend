package com.LevelUpGamer.proyecto.model;
import jakarta.persistence.*; // Asegúrate de usar jakarta si es Spring Boot 3+
import lombok.Data; // Lombok para ahorrarnos getters/setters
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String manufacturer;

    private String distributor;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
package com.LevelUpGamer.proyecto.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author; // Nombre de quien escribe la reseña
    private int rating; // Calificación (ej: 1 a 5)

    @Column(columnDefinition = "TEXT")
    private String comment; // Comentario

    /*
     * RELACIÓN: Muchas Reseñas (Review) pertenecen a un Producto (Product).
     * - '@JoinColumn': Especifica la columna (foreign key) en la tabla 'reviews'
     * que nos conectará con la tabla 'products'.
     * - '@JsonIgnore': ¡MUY IMPORTANTE! Evita un bucle infinito cuando
     * Spring intente convertir esto a JSON (Producto -> Reviews -> Producto -> ...)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // Esencial para evitar bucles en la respuesta JSON
    private Product product;
}
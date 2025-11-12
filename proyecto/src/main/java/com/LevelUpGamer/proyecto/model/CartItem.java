package com.LevelUpGamer.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El carrito al que pertenece (Muchos items pertenecen a Un carrito)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore // Evita bucles
    private Cart cart;

    // El producto que se está comprando (Muchos items pueden ser el Mismo producto)
    @ManyToOne(fetch = FetchType.EAGER) // Queremos ver la info del producto
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;
}

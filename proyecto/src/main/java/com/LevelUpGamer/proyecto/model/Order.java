package com.LevelUpGamer.proyecto.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders") // "order" es una palabra reservada en SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double originalPrice; // Precio sin descuento

    @Column(nullable = false)
    private Double finalPrice; // Precio con descuento (el que se pagó)

    private int pointsEarned; // Puntos ganados en esta compra

    // Un pedido tiene muchos "items"
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL, // Si borro el pedido, se borran sus items
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    // Constructor simple
    public Order() {
        this.orderDate = LocalDateTime.now();
    }
}
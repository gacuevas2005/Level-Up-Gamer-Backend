package com.LevelUpGamer.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @JsonProperty("items")
    private List<CartItem> cartItems = new ArrayList<>();

    @JsonProperty("total")
    public Double getTotalPrice() {
        if (this.cartItems == null || this.cartItems.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (CartItem item : this.cartItems) {
            if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}
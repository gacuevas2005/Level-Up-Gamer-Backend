package com.LevelUpGamer.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();


    // --- ¡NUEVO MÉTODO! ---
    /**
     * Este método no es un campo en la BD (es "transient" por naturaleza).
     * El conversor de JSON (Jackson) lo ejecutará automáticamente
     * y añadirá un campo "totalPrice" al JSON que se envía al frontend.
     * @return El precio total calculado de todos los items en el carrito.
     */
    public Double getTotalPrice() {
        if (this.cartItems == null || this.cartItems.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (CartItem item : this.cartItems) {
            // Nos aseguramos de que el producto y el precio no sean nulos
            if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}

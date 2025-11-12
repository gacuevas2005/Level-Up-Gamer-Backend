package com.LevelUpGamer.proyecto.dto;

import lombok.Data;

// DTO (Data Transfer Object) para las peticiones de añadir/actualizar
@Data
public class CartItemRequest {
    private Long productId;
    private int quantity;
}

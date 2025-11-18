package com.LevelUpGamer.proyecto.dto;


import lombok.Data;

@Data
public class RedeemRequest {
    // Usaremos el ID del Producto que se quiere canjear
    private Long productId;
}
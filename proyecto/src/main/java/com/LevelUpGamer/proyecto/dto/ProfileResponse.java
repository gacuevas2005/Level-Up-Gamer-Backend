package com.LevelUpGamer.proyecto.dto;

import com.LevelUpGamer.proyecto.model.User;
import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private boolean receiveNotifications;
    private String profilePictureUrl;
    private String userRole;
    private int pointsBalance;
    private int userLevel;
    private int totalPointsEarned; // Total para la barra de progreso

    // Creamos un constructor que "mapea"
    // un User a un ProfileResponse, quitando la data sensible.
    public ProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.receiveNotifications = user.isReceiveNotifications();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.userRole = user.getUserRole();
        this.pointsBalance = user.getPointsBalance();
        this.userLevel = user.getUserLevel();
        this.totalPointsEarned = user.getTotalPointsEarned();

    }
}
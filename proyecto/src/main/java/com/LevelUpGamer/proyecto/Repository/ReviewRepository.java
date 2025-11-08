package com.LevelUpGamer.proyecto.Repository;
import com.LevelUpGamer.proyecto.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Esto es todo lo que necesitas.
    // Spring Data JPA se encarga del resto.
}
package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, String> {

    // Torneos por estado
    List<Torneo> findByEstado(String estado);

    // Torneos por múltiples estados
    List<Torneo> findByEstadoIn(List<String> estados);

    // Torneos creados por un usuario (admin / subadmin)
    List<Torneo> findByCreadoPor(String creadoPor);

}

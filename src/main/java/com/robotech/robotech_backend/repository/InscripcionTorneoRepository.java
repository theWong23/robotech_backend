package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionTorneoRepository extends JpaRepository<InscripcionTorneo, String> {

    List<InscripcionTorneo> findByCategoriaTorneoTorneoIdTorneo(String idCategoriaTorneo);

    boolean existsByRobotIdRobotAndCategoriaTorneoTorneoIdTorneo(String idRobot, String idTorneo);

    long countByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);

    // 🔹 Para vista CLUB
    List<InscripcionTorneo> findByRobotCompetidorClubUsuarioIdUsuario(String idUsuarioClub);

    // 🔹 Para vista COMPETIDOR
    List<InscripcionTorneo> findByRobotCompetidorUsuarioIdUsuario(String idUsuario);
    List<InscripcionTorneo> findByCategoriaTorneoIdCategoriaTorneo(String idCategoriaTorneo);
}

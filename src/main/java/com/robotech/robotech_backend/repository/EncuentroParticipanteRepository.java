package com.robotech.robotech_backend.repository;

import com.robotech.robotech_backend.model.EncuentroParticipante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncuentroParticipanteRepository
        extends JpaRepository<EncuentroParticipante, String> {

    List<EncuentroParticipante> findByEncuentroIdEncuentro(String idEncuentro);
}

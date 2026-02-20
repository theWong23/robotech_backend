package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.service.TorneoService;
import com.robotech.robotech_backend.service.TorneoCompetidorService;
import com.robotech.robotech_backend.model.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/competidor/torneos")
public class TorneoCompetidorController {

    private final TorneoService torneoService;
    private final TorneoCompetidorService torneoCompetidorService;

    // --------------------------------------------------
    // LISTAR MIS TORNEOS (COMPETIDOR)
    // --------------------------------------------------
    @GetMapping("/mis")
    @PreAuthorize("hasAuthority('COMPETIDOR')")
    public ResponseEntity<?> listarMis(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(
                torneoCompetidorService.listarMisTorneos(usuario.getIdUsuario())
        );
    }

    // --------------------------------------------------
    // LISTAR TORNEOS DISPONIBLES PARA COMPETIDOR
    // --------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyAuthority('COMPETIDOR','CLUB')")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(
                torneoService.listarPublicos()
        );
    }

    // --------------------------------------------------
    // LISTAR CATEGORÍAS DE UN TORNEO
    // --------------------------------------------------
    @GetMapping("/{id}/categorias")
    @PreAuthorize("hasAnyAuthority('COMPETIDOR','CLUB')")
    public ResponseEntity<?> categorias(@PathVariable String id) {
        return ResponseEntity.ok(
                torneoService.listarCategorias(id)
        );
    }
}


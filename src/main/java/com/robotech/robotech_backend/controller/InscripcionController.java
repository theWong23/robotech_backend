package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.InscripcionDTO;
import com.robotech.robotech_backend.service.InscripcionTorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InscripcionController {

    private final InscripcionTorneoService inscripcionService;

    // Competidor se inscribe a un torneo
    @PostMapping("/{id}/inscribir")
    @PreAuthorize("hasAuthority('COMPETIDOR')")
    public ResponseEntity<?> inscribir(
            @PathVariable String id,                // idCategoriaTorneo
            @RequestBody InscripcionDTO dto,
            Authentication auth
    ) {
        if (auth == null || !(auth.getPrincipal() instanceof com.robotech.robotech_backend.model.Usuario usuario)) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        return ResponseEntity.ok(
                inscripcionService.inscribir(id, dto.getIdRobot(), usuario.getIdUsuario())
        );
    }

    // Listar inscritos de un torneo
    @GetMapping("/{id}/inscritos")
    public ResponseEntity<?> inscritos(@PathVariable String id) {
        return ResponseEntity.ok(inscripcionService.listarInscritos(id));
    }
}

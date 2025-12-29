package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.CompetidorPerfilDTO;
import com.robotech.robotech_backend.model.Competidor;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;

import com.robotech.robotech_backend.service.CompetidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/competidores")
@CrossOrigin("*")
public class CompetidorController {

    @Autowired
    private CompetidorRepository competidorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompetidorService competidorService;


    // ✔ LISTAR COMPETIDORES DE UN CLUB
    @GetMapping("/club/{idClub}")
    public List<Competidor> listarPorClub(@PathVariable String idClub) {
        return competidorRepository.findByClub_IdClub(idClub);
    }

    // Obtener perfil del competidor
    @GetMapping("/{idCompetidor}")
    public ResponseEntity<CompetidorPerfilDTO> obtenerPerfil(
            @PathVariable String idCompetidor
    ) {
        return ResponseEntity.ok(
                competidorService.obtenerPerfil(idCompetidor)
        );
    }

    // ✔ APROBAR COMPETIDOR
    @PutMapping("/{idCompetidor}/aprobar")
    public ResponseEntity<?> aprobarCompetidor(@PathVariable String idCompetidor) {

        Competidor c = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        c.setEstadoValidacion("APROBADO");
        competidorRepository.save(c);

        Usuario u = c.getUsuario();
        u.setEstado("ACTIVO");
        usuarioRepository.save(u);

        return ResponseEntity.ok("Competidor aprobado");
    }

    // ✔ RECHAZAR COMPETIDOR
    @PutMapping("/{idCompetidor}/rechazar")
    public ResponseEntity<?> rechazarCompetidor(@PathVariable String idCompetidor) {

        Competidor c = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        c.setEstadoValidacion("RECHAZADO");
        competidorRepository.save(c);

        Usuario u = c.getUsuario();
        u.setEstado("INACTIVO");
        usuarioRepository.save(u);

        return ResponseEntity.ok("Competidor rechazado");
    }

    @PostMapping("/{idCompetidor}/foto")
    public ResponseEntity<?> subirFoto(
            @PathVariable String idCompetidor,
            @RequestParam("foto") MultipartFile foto
    ) {
        String url = competidorService.subirFoto(idCompetidor, foto);
        return ResponseEntity.ok(
                Map.of("fotoUrl", url)
        );
    }

}

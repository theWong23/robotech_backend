package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.model.entity.Club;
import com.robotech.robotech_backend.model.entity.Competidor;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.EstadoValidacion;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubes")
@CrossOrigin(origins = "${app.frontend.url}")
public class ClubController {

    private final ClubService clubService;
    private final UsuarioRepository usuarioRepository;
    private final CompetidorRepository competidorRepository;

    public ClubController(ClubService clubService,
                          UsuarioRepository usuarioRepository,
                          CompetidorRepository competidorRepository) {
        this.clubService = clubService;
        this.usuarioRepository = usuarioRepository;
        this.competidorRepository = competidorRepository;
    }

    @GetMapping
    public List<Club> listar() {
        return clubService.listar();
    }

    @GetMapping("/{id}")
    public Optional<Club> obtener(@PathVariable String id) {
        return clubService.obtener(id);
    }

    @PostMapping
    public Club crear(@RequestBody Club club) {
        return clubService.crear(club);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        clubService.eliminar(id);
    }

    @GetMapping("/mi-club")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<Club> obtenerMiClub(Authentication auth) {
        return ResponseEntity.ok(clubService.obtenerPorUsuario(auth));
    }

    @PostMapping("/mi-club/imagen")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<Map<String, String>> subirImagenMiClub(
            Authentication auth,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        MultipartFile archivo = file != null ? file : (imagen != null ? imagen : foto);
        String imagenUrl = clubService.subirImagenClub(auth, archivo);
        return ResponseEntity.ok(Map.of("imagenUrl", imagenUrl));
    }

    @GetMapping("/{idClub}/stats")
    @PreAuthorize("hasAuthority('CLUB')")
    public ResponseEntity<Map<String, Long>> getStats(@PathVariable String idClub) {
        return ResponseEntity.ok(clubService.obtenerEstadisticasDashboard(idClub));
    }

    @PreAuthorize("hasAuthority('CLUB')")
    @PutMapping("/aprobar/{idCompetidor}")
    public ResponseEntity<?> aprobar(@PathVariable String idCompetidor) {
        Competidor c = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

        c.setEstadoValidacion(EstadoValidacion.APROBADO);
        competidorRepository.save(c);

        Usuario u = c.getUsuario();
        u.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(u);

        return ResponseEntity.ok(Collections.singletonMap("message", "Competidor aprobado y usuario activado"));
    }
}

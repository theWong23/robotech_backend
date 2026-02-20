package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final RobotRepository robotRepository;
    private final CodigoRegistroCompetidorRepository codigoRegistroCompetidorRepository;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    public List<Club> listar() {
        return clubRepository.findAll();
    }

    public Optional<Club> obtener(String id) {
        return clubRepository.findById(id);
    }

    public Club crear(Club club) {
        Usuario usuarioReal = usuarioRepository.findById(club.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));
        club.setUsuario(usuarioReal);
        return clubRepository.save(club);
    }

    public void eliminar(String id) {
        clubRepository.deleteById(id);
    }

    public Club obtenerPorUsuario(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return clubRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Club no asociado al usuario"));
    }

    public String subirImagenClub(Authentication auth, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Debes seleccionar una imagen");
        }

        Club club = obtenerPorUsuario(auth);

        try {
            String nombreOriginal = file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagen.jpg";
            String nombreArchivo = UUID.randomUUID() + "_" + nombreOriginal;
            Path ruta = Paths.get(uploadsDir, "clubes", nombreArchivo);

            Files.createDirectories(ruta.getParent());
            Files.write(ruta, file.getBytes());

            String imagenUrl = "/uploads/clubes/" + nombreArchivo;
            club.setImagenUrl(imagenUrl);
            clubRepository.save(club);
            return imagenUrl;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen del club");
        }
    }

    public Map<String, Long> obtenerEstadisticasDashboard(String idClub) {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalCompetidores", usuarioRepository.contarUsuariosPorClub(idClub));
        stats.put("totalRobots", robotRepository.contarRobotsPorClub(idClub));

        long total = codigoRegistroCompetidorRepository.countByClubIdClub(idClub);
        stats.put("totalCodigos", total);

        return stats;
    }
}

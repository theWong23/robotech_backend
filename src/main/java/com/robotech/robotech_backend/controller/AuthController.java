package com.robotech.robotech_backend.controller;

import com.robotech.robotech_backend.dto.LoginRequest;
import com.robotech.robotech_backend.dto.RegistroClubDTO;
import com.robotech.robotech_backend.dto.RegistroCompetidorDTO;
import com.robotech.robotech_backend.dto.RegistroJuezDTO;
import com.robotech.robotech_backend.model.*;
import com.robotech.robotech_backend.repository.*;
import com.robotech.robotech_backend.service.AuthService;
import com.robotech.robotech_backend.service.CodigoRegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final ClubRepository clubRepository;
    private final CodigoRegistroService codigoService;
    private final CompetidorRepository competidorRepository;
    private final JuezRepository juezRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Map<String, Object> response = authService.login(
                request.getCorreo(),
                request.getContrasena()
        );

        String rol = (String) response.get("rol");

        // BLOQUEAMOS ROLES ADMIN Y SUBADMIN PARA ESTE LOGIN
        if (rol.equals("ADMINISTRADOR") || rol.equals("SUBADMINISTRADOR")) {
            return ResponseEntity.status(403).body("Este login es solo para Club, Competidor y Juez");
        }

        return ResponseEntity.ok(response);
    }


    // -------------------------------------------------------
    // REGISTRO DE COMPETIDOR (CON SOLICITUD PENDIENTE)
    // -------------------------------------------------------
    @PostMapping("/registro/competidor")
    public ResponseEntity<?> registrarCompetidor(@RequestBody RegistroCompetidorDTO dto) {

        // 1. Validar código
        CodigoRegistroCompetidor codigo = codigoService.validarCodigo(dto.getCodigoClub());

        // 2. Crear usuario
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            return ResponseEntity.badRequest().body("Correo ya registrado");
        }

        if (usuarioRepository.existsByTelefono(dto.getTelefono())) {
            return ResponseEntity.badRequest().body("Teléfono ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol("COMPETIDOR")
                .estado("PENDIENTE")
                .build();

        usuarioRepository.save(usuario);

        // 3. Crear competidor
        Competidor competidor = Competidor.builder()
                .nombres(dto.getNombre())
                .apellidos(dto.getApellido())
                .dni(dto.getDni())
                .club(codigo.getClub())
                .usuario(usuario)
                .estadoValidacion("PENDIENTE")
                .build();

        competidorRepository.save(competidor);

        // 4. Marcar código como usado
        codigoService.marcarUso(codigo);

        return ResponseEntity.ok("Registro enviado. El club debe aprobar tu ingreso.");
    }


    // -------------------------------------------------------
    // REGISTRO DE CLUB (PENDIENTE DE ADMIN)
    // -------------------------------------------------------
    @PostMapping("/registro/club")
    public ResponseEntity<?> registrarClub(@RequestBody RegistroClubDTO dto) {

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            return ResponseEntity.badRequest().body("El correo ya está registrado.");
        }

        // Crear usuario para club
        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol("CLUB")
                .estado("PENDIENTE")
                .build();

        usuarioRepository.save(usuario);

        // Código del club (solo decorativo)
        String codigoClub = "CLB-" + usuario.getIdUsuario().substring(0, 6).toUpperCase();

        // Crear club
        Club club = Club.builder()
                .nombre(dto.getNombre())
                .correoContacto(dto.getCorreo())
                .telefonoContacto(dto.getTelefono())
                .direccionFiscal(dto.getDireccionFiscal())
                .estado("PENDIENTE")
                .codigoClub(codigoClub)
                .usuario(usuario)
                .build();

        clubRepository.save(club);

        return ResponseEntity.ok("Solicitud enviada. Pendiente de aprobación del administrador.");
    }

    // -------------------------------------------------------
    // REGISTRO DE JUEZ (PENDIENTE DE ADMIN)
    // -------------------------------------------------------
    @PostMapping("/registro/juez")
    public ResponseEntity<?> registrarJuez(@RequestBody RegistroJuezDTO dto) {

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            return ResponseEntity.badRequest().body("El correo ya está registrado.");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol("JUEZ")
                .estado("PENDIENTE")
                .build();

        usuarioRepository.save(usuario);

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia(dto.getLicencia())
                .estadoValidacion("PENDIENTE")
                .build();

        juezRepository.save(juez);

        return ResponseEntity.ok("Solicitud enviada. Pendiente de validación del admin.");
    }

}


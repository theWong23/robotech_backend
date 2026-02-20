package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.*;
import com.robotech.robotech_backend.exception.InvalidPasswordResetTokenException;
import com.robotech.robotech_backend.exception.UserNotFoundException;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;

import com.robotech.robotech_backend.repository.UsuarioRepository;
import com.robotech.robotech_backend.repository.ClubRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.repository.JuezRepository;

import com.robotech.robotech_backend.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final ClubRepository clubRepo;
    private final CompetidorRepository competidorRepo;
    private final JuezRepository juezRepo;
    private final CodigoRegistroService codigoService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenGeneratorService tokenGeneratorService; // Inyectar el nuevo servicio
    private final EmailService emailService; // Inyectar EmailService
    private final com.robotech.robotech_backend.service.validadores.DniValidator dniValidator;
    private final com.robotech.robotech_backend.service.validadores.TelefonoValidator telefonoValidator;

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    public LoginResponseDTO login(String correo, String contrasena) {

        boolean correoVacio = correo == null || correo.isBlank();
        boolean contrasenaVacia = contrasena == null || contrasena.isBlank();

        if (correoVacio && contrasenaVacia) {
            throw new RuntimeException("Usuario y contraseña vacíos");
        }
        if (correoVacio) {
            throw new RuntimeException("Usuario vacío");
        }
        if (contrasenaVacia) {
            throw new RuntimeException("Contraseña vacía");
        }

        Usuario usuario = usuarioRepo.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new RuntimeException("Cuenta inactiva");
        }

        Set<RolUsuario> roles = usuario.getRoles() != null ? usuario.getRoles() : java.util.Set.of();
        if (roles.contains(RolUsuario.ADMINISTRADOR)) {
            throw new RuntimeException("Este inicio de sesion es solo para club, competidor, juez y subadministrador");
        }

        String token = jwtService.generarToken(usuario);

        Map<String, Object> entidad = new HashMap<>();

        if (roles.contains(RolUsuario.CLUB)) {
            Club club = clubRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Club no encontrado"));

            if (club.getEstado() != EstadoClub.ACTIVO) {
                throw new RuntimeException("Club inactivo");
            }

            entidad.put("club", new ClubLoginDTO(
                    club.getIdClub(),
                    club.getNombre(),
                    club.getCorreoContacto(),
                    club.getTelefonoContacto(),
                    club.getImagenUrl()
            ));
        }

        if (roles.contains(RolUsuario.COMPETIDOR)) {
            Competidor c = competidorRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));

            if (c.getEstadoValidacion() != EstadoValidacion.APROBADO) {
                throw new RuntimeException("Competidor no aprobado");
            }

            boolean clubValido = c.getClubActual() != null && c.getClubActual().getEstado() == EstadoClub.ACTIVO;
            boolean libre = c.getClubActual() == null;
            if (!clubValido && !roles.contains(RolUsuario.JUEZ) && !libre) {
                throw new RuntimeException("Club inactivo");
            }

            entidad.put("competidor", new CompetidorLoginDTO(
                    c.getIdCompetidor(),
                    usuario.getNombres(),
                    usuario.getApellidos(),
                    usuario.getCorreo(),
                    c.getClubActual() != null ? c.getClubActual().getIdClub() : null,
                    c.getClubActual() != null ? c.getClubActual().getNombre() : "Agente libre"
            ));
        }

        if (roles.contains(RolUsuario.JUEZ)) {
            Juez j = juezRepo.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

            entidad.put("juez", Map.of(
                    "idJuez", j.getIdJuez(),
                    "correo", usuario.getCorreo()
            ));
        }

        if (roles.contains(RolUsuario.ADMINISTRADOR) || roles.contains(RolUsuario.SUBADMINISTRADOR)) {
            entidad.put("usuario", usuario);
        }

        if (entidad.isEmpty()) {
            entidad.put("usuario", usuario);
        }

        return new LoginResponseDTO(
                token,
                roles,
                entidad
        );
    }

    // -------------------------------------------------------
    // SOLICITAR RESTABLECIMIENTO DE CONTRASEÑA
    // -------------------------------------------------------
    @Transactional
    public void requestPasswordReset(String email) {
        Usuario usuario = usuarioRepo.findByCorreo(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ese correo electrónico."));

        String token = tokenGeneratorService.generateSecureToken();
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusHours(1)); // Token válido por 1 hora
        usuarioRepo.save(usuario);

        emailService.sendPasswordResetEmail(email, token);
    }

    // -------------------------------------------------------
    // RESTABLECER CONTRASEÑA
    // -------------------------------------------------------
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepo.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Token de restablecimiento inválido."));

        if (usuario.getPasswordResetTokenExpiryDate() == null || usuario.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordResetTokenException("Token de restablecimiento caducado.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(newPassword));
        usuario.setPasswordResetToken(null); // Invalida el token
        usuario.setPasswordResetTokenExpiryDate(null); // Elimina la fecha de caducidad
        usuarioRepo.save(usuario);
    }


    // -------------------------------------------------------
    // REGISTRO COMPETIDOR
    // -------------------------------------------------------
    @Transactional
    public void registrarCompetidor(RegistroCompetidorDTO dto) {

        dniValidator.validar(dto.getDni());
        telefonoValidator.validar(dto.getTelefono());

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        if (usuarioRepo.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Teléfono ya registrado");
        }

        CodigoRegistroCompetidor codigo =
                codigoService.validarCodigo(dto.getCodigoClub());

        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombres(dto.getNombre())
                .apellidos(dto.getApellido())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.COMPETIDOR))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        Competidor competidor = Competidor.builder()
                .usuario(usuario)
                .clubActual(codigo.getClub())
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        competidorRepo.save(competidor);

        codigoService.marcarUso(codigo);
    }

    // -------------------------------------------------------
    // REGISTRO CLUB
    // -------------------------------------------------------
    @Transactional
    public void registrarClub(RegistroClubDTO dto) {

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            telefonoValidator.validar(dto.getTelefono());
        }

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.CLUB))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        String codigoClub = "CLB-" +
                usuario.getIdUsuario().substring(0, 6).toUpperCase();

        Club club = Club.builder()
                .nombre(dto.getNombre())
                .correoContacto(dto.getCorreo())
                .telefonoContacto(dto.getTelefono())
                .direccionFiscal(dto.getDireccionFiscal())
                .estado(EstadoClub.PENDIENTE)
                .codigoClub(codigoClub)
                .usuario(usuario)
                .build();

        clubRepo.save(club);
    }

    // -------------------------------------------------------
    // REGISTRO JUEZ
    // -------------------------------------------------------
    @Transactional
    public void registrarJuez(RegistroJuezDTO dto) {

        dniValidator.validar(dto.getDni());
        telefonoValidator.validar(dto.getTelefono());

        if (usuarioRepo.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .roles(Set.of(RolUsuario.JUEZ))
                .estado(EstadoUsuario.PENDIENTE)
                .build();

        usuarioRepo.save(usuario);

        Juez juez = Juez.builder()
                .usuario(usuario)
                .licencia(dto.getLicencia())
                .estadoValidacion(EstadoValidacion.PENDIENTE)
                .build();

        juezRepo.save(juez);
    }
}



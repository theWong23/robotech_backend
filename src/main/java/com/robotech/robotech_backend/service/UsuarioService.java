package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.CrearAdminDTO;
import com.robotech.robotech_backend.dto.CrearUsuarioDTO;
import com.robotech.robotech_backend.model.entity.*;
import com.robotech.robotech_backend.model.enums.*;
import com.robotech.robotech_backend.repository.CodigoRegistroCompetidorRepository;
import com.robotech.robotech_backend.repository.CompetidorRepository;
import com.robotech.robotech_backend.model.enums.EstadoUsuario;
import com.robotech.robotech_backend.model.enums.RolUsuario;
import com.robotech.robotech_backend.model.entity.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CompetidorRepository competidorRepository;
    private final CodigoRegistroCompetidorRepository codigoRepo;
    private final PasswordEncoder passwordEncoder;
    private final com.robotech.robotech_backend.service.validadores.NicknameValidator nicknameValidator;
    private final com.robotech.robotech_backend.service.validadores.DniValidator dniValidator;
    private final com.robotech.robotech_backend.service.validadores.TelefonoValidator telefonoValidator;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public Usuario crearUsuario(CrearUsuarioDTO dto, String codigoIngresado) {
        // --- 1. VALIDACIONES BÁSICAS ---
        nicknameValidator.validar(dto.nombres());
        nicknameValidator.validar(dto.apellidos());
        dniValidator.validar(dto.dni());
        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            telefonoValidator.validar(dto.telefono());
        }


        if (usuarioRepository.existsByCorreo(dto.correo())) throw new RuntimeException("Correo registrado");
        if (usuarioRepository.existsByTelefono(dto.telefono())) throw new RuntimeException("Teléfono registrado");
        if (usuarioRepository.existsByDni(dto.dni())) {
            throw new RuntimeException("DNI registrado");
        }

        // --- 2. VALIDACIÓN DEL CÓDIGO DE REGISTRO ---
        String codigoLimpio = codigoIngresado != null ? codigoIngresado.trim() : "";

        CodigoRegistroCompetidor codigoEntidad = codigoRepo.findByCodigo(codigoLimpio)
                .orElseThrow(() -> new RuntimeException("El código de registro no existe: " + codigoLimpio));

        if (codigoEntidad.getExpiraEn() != null && codigoEntidad.getExpiraEn().before(new Date())) {
            throw new RuntimeException("Este código de invitación ha expirado.");
        }

        if (codigoEntidad.getUsosActuales() >= codigoEntidad.getLimiteUso()) {
            throw new RuntimeException("Este código ya alcanzó su límite de usos.");
        }

        Club club = codigoEntidad.getClub();

        codigoEntidad.setUsosActuales(codigoEntidad.getUsosActuales() + 1);
        if (codigoEntidad.getUsosActuales() >= codigoEntidad.getLimiteUso()) {
            codigoEntidad.setUsado(true);
        }
        codigoRepo.save(codigoEntidad);

        // --- 3. CREAR USUARIO (CORREGIDO) ---
        Usuario usuario = Usuario.builder()
                .dni(dto.dni())
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                // CORRECCIÓN: Usar el Enum RolUsuario.COMPETIDOR, no un String ni ADMINISTRADOR
                .roles(Set.of(RolUsuario.COMPETIDOR))
                // CORRECCIÓN: Dejar solo un estado (ACTIVO para que pueda loguearse)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario uGuardado = usuarioRepository.save(usuario);

        // --- 4. CREAR COMPETIDOR ---
        Competidor competidor = new Competidor();
        competidor.setUsuario(uGuardado);
        competidor.setClubActual(club);
        competidor.setEstadoValidacion(EstadoValidacion.PENDIENTE);

        competidorRepository.save(competidor);

        return uGuardado;
    }

    @Transactional
    public Usuario actualizarUsuario(String id, CrearUsuarioDTO dto) {
        Usuario usuario = obtenerPorId(id);
        dniValidator.validar(dto.dni());
        if (dto.telefono() != null && !dto.telefono().isBlank()) {
            telefonoValidator.validar(dto.telefono());
        }
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setCorreo(dto.correo());
        usuario.setTelefono(dto.telefono());
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> login(String correo, String contrasena) {
        return usuarioRepository.findByCorreo(correo)
                // ⚠️ Asegúrate de usar == para comparar Enums o .equals()
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .filter(u -> passwordEncoder.matches(contrasena, u.getContrasenaHash()));
    }

    public void eliminarUsuario(String id) {
        Usuario u = obtenerPorId(id);
        u.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(u);
    }

    @Transactional
    public Usuario crearAdministrador(CrearAdminDTO dto) {
        if (usuarioRepository.existsByRolesContaining(RolUsuario.ADMINISTRADOR)) {
            throw new RuntimeException("Ya existe un administrador en el sistema");
        }

        if (usuarioRepository.existsByCorreo(dto.correo())) {
            throw new RuntimeException("Correo registrado");
        }

        if (usuarioRepository.existsByTelefono(dto.telefono())) {
            throw new RuntimeException("Teléfono registrado");
        }

        if (usuarioRepository.existsByDni(dto.dni())) {
            throw new RuntimeException("DNI registrado");
        }

        Usuario admin = Usuario.builder()
                .dni(dto.dni())
                .nombres(dto.nombres())
                .apellidos(dto.apellidos())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .contrasenaHash(passwordEncoder.encode(dto.contrasena()))
                .roles(Set.of(RolUsuario.ADMINISTRADOR))          // 👈 FIJO
                .estado(EstadoUsuario.ACTIVO)
                .build();

        return usuarioRepository.save(admin);
    }

}



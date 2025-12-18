package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.dto.JuezDTO;
import com.robotech.robotech_backend.model.Juez;
import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.JuezRepository;
import com.robotech.robotech_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminJuezService {

    private final JuezRepository juezRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------
    // CREAR JUEZ
    // ---------------------------------------------------------
    public Juez crear(JuezDTO dto) {

        Usuario u = Usuario.builder()
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .contrasenaHash(passwordEncoder.encode(dto.getContrasena()))
                .rol("JUEZ")
                .estado("ACTIVO")
                .build();

        usuarioRepository.save(u);

        Juez j = Juez.builder()
                .usuario(u)
                .licencia(dto.getLicencia())
                .estadoValidacion("PENDIENTE")
                .creadoPor(dto.getCreadoPor())
                .creadoEn(new Date())
                .build();

        return juezRepository.save(j);
    }

    // ---------------------------------------------------------
    // LISTAR
    // ---------------------------------------------------------
    public List<Juez> listar() {
        return juezRepository.findAll();
    }

    // ---------------------------------------------------------
    // EDITAR JUEZ
    // ---------------------------------------------------------
    public Juez editar(String id, JuezDTO dto) {

        Juez j = juezRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        Usuario u = j.getUsuario();
        u.setCorreo(dto.getCorreo());
        u.setTelefono(dto.getTelefono());

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            u.setContrasenaHash(passwordEncoder.encode(dto.getContrasena()));
        }

        usuarioRepository.save(u);

        j.setLicencia(dto.getLicencia());

        return juezRepository.save(j);
    }

    // ---------------------------------------------------------
    // ELIMINAR JUEZ
    // ---------------------------------------------------------
    public void eliminar(String id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juez no existe"));
        juezRepository.delete(juez); // gracias a orphanRemoval borra al usuario
    }

    // ---------------------------------------------------------
    // APROBAR JUEZ
    // ---------------------------------------------------------
    public Juez aprobar(String idJuez, String adminId) {

        Juez j = juezRepository.findById(idJuez)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        j.setEstadoValidacion("APROBADO");
        j.setValidadoPor(adminId);
        j.setValidadoEn(new Date());

        return juezRepository.save(j);
    }

    // ---------------------------------------------------------
    // RECHAZAR JUEZ
    // ---------------------------------------------------------
    public Juez rechazar(String idJuez, String adminId) {

        Juez j = juezRepository.findById(idJuez)
                .orElseThrow(() -> new RuntimeException("Juez no encontrado"));

        j.setEstadoValidacion("RECHAZADO");
        j.setValidadoPor(adminId);
        j.setValidadoEn(new Date());

        return juezRepository.save(j);
    }
}

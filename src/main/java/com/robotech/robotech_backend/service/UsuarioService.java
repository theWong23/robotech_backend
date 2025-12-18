package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.model.Usuario;
import com.robotech.robotech_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crearUsuario(Usuario usuario) {
        usuario.setContrasenaHash(passwordEncoder.encode(usuario.getContrasenaHash()));
        return usuarioRepository.save(usuario);
    }

    public boolean correoExiste(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }


    public boolean telefonoExiste(String telefono) {
        return usuarioRepository.existsByTelefono(telefono);
    }

    public Optional<Usuario> login(String correo, String contrasena) {
        return usuarioRepository.findByCorreo(correo)
                .filter(usuario -> passwordEncoder.matches(contrasena, usuario.getContrasenaHash()));
    }

    public Optional<Usuario> actualizarUsuario(String id, Usuario datos) {
        return usuarioRepository.findById(id).map(u -> {
            u.setCorreo(datos.getCorreo());
            u.setTelefono(datos.getTelefono());
            u.setContrasenaHash(passwordEncoder.encode(datos.getContrasenaHash()));
            u.setRol(datos.getRol());
            u.setEstado(datos.getEstado());
            return usuarioRepository.save(u);
        });
    }

    public boolean eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            return false;
        }
        usuarioRepository.deleteById(id);
        return true;
    }


}

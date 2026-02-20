package com.robotech.robotech_backend.dto;

public record ClubLoginDTO(
        String idClub,
        String nombre,
        String correoContacto,
        String telefonoContacto,
        String imagenUrl
) {}



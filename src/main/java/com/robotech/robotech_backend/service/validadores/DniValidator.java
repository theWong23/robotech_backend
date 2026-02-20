package com.robotech.robotech_backend.service.validadores;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DniValidator {

    private static final String REGEX_DNI = "^\\d{8}$";

    public void validar(String dni) {
        if (dni == null || !dni.matches(REGEX_DNI)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "DNI inválido. Asegúrate de que tenga 8 dígitos."
            );
        }
    }
}



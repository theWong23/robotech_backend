package com.robotech.robotech_backend.service.validadores;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DniValidatorTest {
    private final DniValidator dniValidator = new DniValidator();

    @Test
    void validar_ok() {
        assertDoesNotThrow(() -> dniValidator.validar("12345678"));
    }

    @Test
    void validar_invalido_lanza_error() {
        assertThrows(ResponseStatusException.class, () -> dniValidator.validar("123"));
    }
}

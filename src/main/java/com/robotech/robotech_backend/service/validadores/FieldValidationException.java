package com.robotech.robotech_backend.service.validadores;

import java.util.List;

public class FieldValidationException extends RuntimeException {

    private final String field;
    private final List<String> suggestions;

    // CONSTRUCTOR COMPLETO
    public FieldValidationException(String field, String message, List<String> suggestions) {
        super(message);
        this.field = field;
        this.suggestions = suggestions;
    }

    // CONSTRUCTOR SIMPLE
    public FieldValidationException(String field, String message) {
        this(field, message, List.of());
    }

    public String getField() {
        return field;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}


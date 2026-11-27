package com.medagenda.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Backing bean of the HTML form. A plain JavaBean (not a record) because the form tag library
 * binds through getters and setters; it reuses the same validation rules as the REST API.
 */
public class SpecialtyForm {

    @NotBlank(message = "Informe o nome da especialidade.")
    @Size(max = 80, message = "O nome pode ter no máximo 80 caracteres.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

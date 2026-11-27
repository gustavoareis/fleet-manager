package com.medagenda.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HealthInsuranceForm {

    @NotBlank(message = "Informe o nome do convênio.")
    @Size(max = 80, message = "O nome pode ter no máximo 80 caracteres.")
    private String name;

    /** An unchecked checkbox sends nothing, so a primitive boolean stays false, which is what we want. */
    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

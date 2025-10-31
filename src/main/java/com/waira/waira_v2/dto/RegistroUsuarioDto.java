package com.waira.waira_v2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioDto {
    @NotBlank
    @Size(max = 15)
    private String nombre;

    @NotBlank
    @Size(max = 15)
    private String apellido;

    @NotBlank
    @Size(max = 15)
    private String telefono;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String contraseña;

    @NotBlank
    private String confirmContraseña;

    @NotNull
    private String nombreRol;

    public String getConfirmContraseña() {
        return confirmContraseña;
    }
}

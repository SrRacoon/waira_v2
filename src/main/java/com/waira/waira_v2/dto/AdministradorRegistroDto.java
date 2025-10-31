package com.waira.waira_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdministradorRegistroDto {
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
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String contrasena;

    @NotBlank
    @Size(min = 6, max = 100)
    private String confirmContrasena;
}

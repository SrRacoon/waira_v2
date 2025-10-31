package com.waira.waira_v2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRegistroDto {
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

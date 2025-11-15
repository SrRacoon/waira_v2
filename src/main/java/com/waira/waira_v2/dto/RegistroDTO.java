package com.waira.waira_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {
    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    private String documento;
    private String telefono;
    private String email;
    private String contraseña;
    private String confirmContraseña;
}

package com.waira.waira_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudProveedorDTO {
    private String nit;
    private String razonSocial;
    private String tipoVia;
    private String numero;
    private String complemento;
    private String barrio;
    private String ciudad;
}


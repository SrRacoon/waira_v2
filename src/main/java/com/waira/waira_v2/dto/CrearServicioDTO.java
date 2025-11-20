package com.waira.waira_v2.dto;

import lombok.Data;

@Data
public class CrearServicioDTO {
    private String nombreServicio;
    private String descripcion;
    private Double precio;
    private Integer diasDuracion;
}

package com.waira.waira_v2.dto;

import lombok.Data;

@Data
public class SubcategoriaTreeDTO {
    private Integer id;
    private Integer categoriaId;
    private String nombre;
    private long totalServicios;
}

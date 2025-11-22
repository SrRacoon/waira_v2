package com.waira.waira_v2.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CategoriaTreeDTO {
    private Integer id;
    private String nombre;
    private long totalServicios;
    private List<SubcategoriaTreeDTO> subcategorias = new ArrayList<>();
}

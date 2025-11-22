package com.waira.waira_v2.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CrearServicioDTO {
    private String nombreServicio;
    private String descripcion;
    private Double precio;
    private Integer diasDuracion;
    private String tipoVia;
    private String numero;
    private String complemento;
    private String barrio;
    private String ciudad;
    private List<Integer> categoriasIds = new ArrayList<>();
    private List<Integer> subcategoriasIds = new ArrayList<>();
}

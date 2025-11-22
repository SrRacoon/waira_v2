package com.waira.waira_v2.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ServicioExplorarDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer vistas;
    private Date fechaPublicacion;
    private String ciudad;
    private String proveedor;
    private List<Integer> categoriasIds = new ArrayList<>();
    private List<String> categorias = new ArrayList<>();
    private List<Integer> subcategoriasIds = new ArrayList<>();
    private List<String> subcategorias = new ArrayList<>();
    private String imagenDestacada;
    private String estado;
}

package com.waira.waira_v2.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ServicioDetalleDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String precioTexto;
    private String ciudad;
    private String proveedor;
    private Integer diasDuracion;
    private Integer vistas;
    private Double calificacionPromedio;
    private Long totalResenas;
    private List<String> imagenes = new ArrayList<>();
    private List<String> categorias = new ArrayList<>();
    private List<String> subcategorias = new ArrayList<>();
    private List<ResenaDTO> resenas = new ArrayList<>();
}

package com.waira.waira_v2.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ResenaDTO {
    private String autor;
    private Integer calificacion;
    private Date fecha;
    private String comentario;
}

package com.waira.waira_v2.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "solicitudes_proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;


    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;


    private String nit;
    private String razonSocial;


    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;


    private Date fechaSolicitud;
}

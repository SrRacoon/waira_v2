package com.waira.waira_v2.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;


    private String nombres;
    private String apellidos;
    private String telefono;


    @ManyToOne
    @JoinColumn(name = "id_tipo_identificacion", nullable = false)
    private TipoIdentificacion tipoIdentificacion;


    @Column(nullable = false, unique = true)
    private String numeroIdentificacion;


    private String razonSocial;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String contrasena;


    @ManyToOne
    @JoinColumn(name = "id_direccion")
    private Direccion direccion;


    private Boolean estadoCuenta = true;


    @ManyToMany
    @JoinTable(
    name = "usuarios_roles",
    joinColumns = @JoinColumn(name = "id_usuario"),
    inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private List<Rol> roles;
}




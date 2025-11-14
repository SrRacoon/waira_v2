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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;


    @Column(nullable = false, unique = true)
    private String nombreRol;


    @ManyToMany(mappedBy = "roles")
    private List<Usuario> usuarios;


    @ManyToMany
    @JoinTable(
    name = "rol_permisos",
    joinColumns = @JoinColumn(name = "id_rol"),
    inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    private List<Permiso> permisos;
}

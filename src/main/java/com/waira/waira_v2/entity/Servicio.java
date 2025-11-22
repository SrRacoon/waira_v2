package com.waira.waira_v2.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "servicios")
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idServicio;


    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;


    private String nombreServicio;


    @Column(columnDefinition = "text")
    private String descripcion;


    private Double precio;


    @ManyToOne
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;


    private Integer diasDuracion;


    private Date fechaPublicacion;


    private Integer vistas = 0;


    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    @OrderBy("idImagen ASC")
    private List<Imagen> imagenes;


    @ManyToMany
    @JoinTable(
    name = "servicio_metodo_pago",
    joinColumns = @JoinColumn(name = "id_servicio"),
    inverseJoinColumns = @JoinColumn(name = "id_metodo")
    )
    private List<MetodoPago> metodosPago;


    @ManyToMany
    @JoinTable(
    name = "categorias_servicios",
    joinColumns = @JoinColumn(name = "id_servicio"),
    inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private List<Categoria> categorias;

    @ManyToMany
    @JoinTable(
    name = "subcategorias_servicios",
    joinColumns = @JoinColumn(name = "id_servicio"),
    inverseJoinColumns = @JoinColumn(name = "id_subcategoria")
    )
    private List<Subcategoria> subcategorias;
}

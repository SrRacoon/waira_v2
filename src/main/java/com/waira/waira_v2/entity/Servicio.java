package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idServicio;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false, length = 300)
    private String nombreServicio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;

    @Column(nullable = false)
    private Integer diasDuracion;

    @Temporal(TemporalType.DATE)
    private Date fechaPublicacion = new Date();

    private Integer vistas = 0;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    private List<Imagen> imagenes;

    @ManyToMany
    @JoinTable(
        name = "servicio_metodo_pago",
        joinColumns = @JoinColumn(name = "id_servicio"),
        inverseJoinColumns = @JoinColumn(name = "id_metodo")
    )
    private List<MetodoPago> metodosPago;
}


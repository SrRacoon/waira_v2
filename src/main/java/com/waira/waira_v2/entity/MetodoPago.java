package com.waira.waira_v2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMetodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMetodoPago nombre;
}


